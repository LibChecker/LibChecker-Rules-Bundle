#!/usr/bin/env python3
"""Pin a producer release; never resolves floating latest during normal builds."""
import argparse
import hashlib
import io
import json
import pathlib
import urllib.request
import zipfile

MAX_BYTES = 32 * 1024 * 1024
ROOT = pathlib.Path(__file__).resolve().parents[1]


def read(source, limit):
    if source.startswith('https://'):
        with urllib.request.urlopen(source, timeout=60) as response:
            data = response.read(limit + 1)
    else:
        data = pathlib.Path(source).read_bytes()
    if len(data) > limit:
        raise ValueError('Input exceeds size limit')
    return data


def require(condition, message):
    if not condition:
        raise ValueError(message)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--manifest', required=True)
    parser.add_argument('--root', required=True, help='Producer output directory or HTTPS rules-data root')
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    raw = read(args.manifest, 1024 * 1024)
    manifest = json.loads(raw)
    artifact = manifest['artifacts']['android']
    require(manifest['schemaVersion'] == artifact['schemaVersion'] == 5, 'Unsupported schema')
    require(manifest['minimumReader']['android'] <= 5 and artifact['minimumReaderVersion'] <= 5, 'Unsupported reader')
    require(type(manifest['dataVersion']) is int and manifest['dataVersion'] > 0, 'Invalid data version')
    require(artifact['path'] == f"releases/{manifest['dataVersion']}/android-v5.zip", 'Invalid artifact path')
    archive = read(args.root.rstrip('/') + '/' + artifact['path'], MAX_BYTES)
    require(len(archive) == artifact['size'], 'Archive size mismatch')
    require(hashlib.sha256(archive).hexdigest() == artifact['sha256'], 'Archive checksum mismatch')
    with zipfile.ZipFile(io.BytesIO(archive)) as bundle:
        require(sorted(bundle.namelist()) == ['metadata.json', 'rules.db'], 'Android bundle must contain only SQLite and metadata')
        for entry in bundle.infolist():
            require(not entry.is_dir() and entry.file_size <= (MAX_BYTES if entry.filename == 'rules.db' else 65536), 'Expanded entry exceeds size limit')
        files = {name: bundle.read(name) for name in bundle.namelist()}
        metadata = json.loads(files['metadata.json'])
        require(metadata == {key: value for key, value in manifest.items() if key != 'artifacts'}, 'Metadata mismatch')
    destination = ROOT / 'library/src/main/assets/lcrules/v5'
    lock = ROOT / 'rules/manifest.json'
    if args.check:
        require({p.name for p in destination.iterdir()} == set(files), 'Unexpected bundled assets')
        require(all((destination / name).read_bytes() == data for name, data in files.items()), 'Bundled files differ')
        require(json.loads(lock.read_bytes()) == manifest, 'Bundled manifest differs')
    else:
        if lock.exists():
            previous = json.loads(lock.read_bytes())
            require(manifest['dataVersion'] > previous['dataVersion'] or manifest == previous, 'Use a forward version to change bundled rules')
        destination.mkdir(parents=True, exist_ok=True)
        for name, data in files.items():
            (destination / name).write_bytes(data)
        lock.parent.mkdir(parents=True, exist_ok=True)
        lock.write_text(json.dumps(manifest, indent=2, sort_keys=True) + '\n')
    print(f"Verified rules {manifest['dataVersion']}: {artifact['sha256']}")


if __name__ == '__main__':
    main()
