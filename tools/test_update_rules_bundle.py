import hashlib
import json
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest
import zipfile


class UpdateRulesBundleTest(unittest.TestCase):
    def test_extracts_one_database_and_preserves_immutable_version_checks(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            tool = root / 'tools/update-rules-bundle.py'
            tool.parent.mkdir()
            shutil.copyfile(Path(__file__).with_name('update-rules-bundle.py'), tool)
            producer = root / 'producer'
            archive = producer / 'releases/45/android-v5.zip'
            archive.parent.mkdir(parents=True)
            manifest = producer / 'manifest.json'

            def release(revision):
                metadata = dict(schemaVersion=5, dataVersion=45, minimumReader={'android': 5}, sourceRevision=revision)
                with zipfile.ZipFile(archive, 'w') as output:
                    output.writestr('rules.db', b'producer-db')
                    output.writestr('metadata.json', json.dumps(metadata))
                raw = archive.read_bytes()
                metadata['artifacts'] = {'android': dict(schemaVersion=5, minimumReaderVersion=5,
                    path='releases/45/android-v5.zip', size=len(raw), sha256=hashlib.sha256(raw).hexdigest())}
                manifest.write_text(json.dumps(metadata))

            def run(*extra):
                return subprocess.run([sys.executable, '-B', str(tool), '--manifest', str(manifest),
                    '--root', str(producer), *extra], capture_output=True, text=True)

            release('first')
            self.assertEqual(0, run().returncode)
            self.assertEqual(0, run('--check').returncode)
            assets = root / 'library/src/main/assets/lcrules/v5'
            self.assertEqual({'rules.db', 'metadata.json'}, {p.name for p in assets.iterdir()})
            self.assertTrue((root / 'rules/manifest.json').is_file())
            original = (assets / 'metadata.json').read_bytes()
            release('different-same-version')
            self.assertNotEqual(0, run().returncode)
            self.assertEqual(original, (assets / 'metadata.json').read_bytes())
            archive.write_bytes(b'bad archive')
            self.assertNotEqual(0, run().returncode)


if __name__ == '__main__':
    unittest.main()
