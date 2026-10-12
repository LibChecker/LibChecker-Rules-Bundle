import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


TOOL = Path(__file__).with_name('release-version.py').resolve()


class ReleaseVersionTest(unittest.TestCase):
    def test_new_release_retry_and_immutable_tag(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)

            def git(*args):
                return subprocess.check_output(['git', '-C', temporary, *args], stderr=subprocess.PIPE)

            def release():
                return subprocess.run([sys.executable, str(TOOL)], cwd=root, capture_output=True, text=True)

            git('init', '-q')
            git('config', 'user.name', 'Release test')
            git('config', 'user.email', 'release@example.invalid')
            manifest = root / 'rules/manifest.json'
            manifest.parent.mkdir()
            manifest.write_text(json.dumps({'dataVersion': 46, 'contentSha256': 'first'}))
            git('add', '.')
            git('commit', '-qm', 'baseline')
            self.assertEqual('46\n', release().stdout)
            git('tag', '-a', '46', '-m', 'Rules 46')
            self.assertEqual(0, release().returncode)
            git('commit', '--allow-empty', '-qm', 'unrelated change')
            self.assertEqual(0, release().returncode)
            manifest.write_text(json.dumps({'dataVersion': 46, 'contentSha256': 'changed'}))
            self.assertIn('already pins different rules', release().stderr)
            self.assertNotEqual(0, release().returncode)
            for invalid in (True, 0, -1, '46', '46; echo invalid'):
                manifest.write_text(json.dumps({'dataVersion': invalid}))
                self.assertNotEqual(0, release().returncode)
            manifest.write_text(json.dumps({'dataVersion': 47}))
            result = release()
            self.assertEqual(0, result.returncode, result.stderr)
            self.assertEqual('47\n', result.stdout)


if __name__ == '__main__':
    unittest.main()
