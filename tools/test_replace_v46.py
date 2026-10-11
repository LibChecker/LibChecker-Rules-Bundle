import hashlib
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

spec=importlib.util.spec_from_file_location('bundle_update',Path(__file__).with_name('update-rules-bundle.py'))
update=importlib.util.module_from_spec(spec);spec.loader.exec_module(update)


class ReplacementTest(unittest.TestCase):
    def test_restricted_version_hash_content_and_consumption(self):
        with tempfile.TemporaryDirectory() as tmp:
            lock=Path(tmp)/'manifest.json';lock.write_text(json.dumps(dict(dataVersion=46)))
            checksum=hashlib.sha256(lock.read_bytes()).hexdigest()
            manifest=dict(dataVersion=46,ruleCount=3041,contentSha256=update.NEW_CONTENT_SHA256)
            with patch.object(update,'OLD_LOCK_SHA256',checksum):
                self.assertEqual(46,update.authorize_replacement(lock,manifest)['dataVersion'])
                with self.assertRaisesRegex(ValueError,'restricted'):update.authorize_replacement(lock,{**manifest,'dataVersion':47})
                with self.assertRaisesRegex(ValueError,'content'):update.authorize_replacement(lock,{**manifest,'contentSha256':'other'})
                lock.write_text(json.dumps(manifest))
                with self.assertRaisesRegex(ValueError,'consumed'):update.authorize_replacement(lock,manifest)
