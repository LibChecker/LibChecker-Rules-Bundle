#!/usr/bin/env python3
"""Resolve the data-version tag without allowing a published baseline to change."""
import json
from pathlib import Path
import subprocess


def release_version():
    manifest = json.loads(Path('rules/manifest.json').read_text())
    version = manifest['dataVersion']
    if type(version) is not int or version <= 0:
        raise ValueError('Release dataVersion must be a positive integer')
    tag = f'refs/tags/{version}'
    exists = subprocess.run(['git', 'show-ref', '--verify', '--quiet', tag])
    if exists.returncode not in (0, 1):
        raise RuntimeError('Cannot inspect release tag')
    if exists.returncode == 0:
        subprocess.run(['git', 'merge-base', '--is-ancestor', tag, 'HEAD'], check=True)
        published = subprocess.check_output(['git', 'show', f'{tag}:rules/manifest.json'])
        if json.loads(published) != manifest:
            raise ValueError(f'Tag {version} already pins different rules; use a new dataVersion')
    return str(version)


if __name__ == '__main__':
    print(release_version())
