#!/usr/bin/env python3
"""Resolve the data-version tag without allowing a published baseline to change."""
import argparse
import json
from pathlib import Path
import re
import subprocess


def release_version(requested=''):
    manifest = json.loads(Path('rules/manifest.json').read_text())
    data_version = manifest['dataVersion']
    if type(data_version) is not int or data_version <= 0:
        raise ValueError('Release dataVersion must be a positive integer')
    version = requested or str(data_version)
    if not re.fullmatch(rf"{data_version}(?:\.[1-9][0-9]*)?", version):
        raise ValueError('Release tag must match dataVersion or dataVersion.patch')
    for release in dict.fromkeys((str(data_version), version)):
        tag = f'refs/tags/{release}'
        exists = subprocess.run(['git', 'show-ref', '--verify', '--quiet', tag])
        if exists.returncode not in (0, 1):
            raise RuntimeError('Cannot inspect release tag')
        if exists.returncode == 0:
            subprocess.run(['git', 'merge-base', '--is-ancestor', tag, 'HEAD'], check=True)
            published = subprocess.check_output(['git', 'show', f'{tag}:rules/manifest.json'])
            if json.loads(published) != manifest:
                raise ValueError(f'Tag {release} already pins different rules; use a new dataVersion')
    return version


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('version', nargs='?', default='')
    print(release_version(parser.parse_args().version))
