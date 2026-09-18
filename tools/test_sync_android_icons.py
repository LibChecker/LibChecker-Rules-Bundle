import unittest

from sync_android_icons import DRAWABLES, MAP, generate


class SyncIconsTest(unittest.TestCase):
    def test_stable_indexes_web_fallback_and_validation(self):
        index = {'-1': {'iconId': None, 'isSimpleColorIcon': True},
                 '0': {'iconId': 'ic_lib_old', 'isSimpleColorIcon': False},
                 '1': {'iconId': 'ic_lib_web', 'isSimpleColorIcon': False}}
        xml = b'<vector xmlns:android="http://schemas.android.com/apk/res/android"><path/></vector>'
        previous = '/* 0 */ R.drawable.ic_lib_old,'
        output = generate(index, {'ic_lib_old': xml}, previous, 'a' * 40)
        self.assertEqual(xml, output[DRAWABLES / 'ic_lib_old.xml'])
        self.assertIn('/* 1: ic_lib_web */ R.drawable.ic_sdk_placeholder', output[MAP].decode())
        self.assertNotIn(DRAWABLES / 'ic_lib_web.xml', output)
        # Generated comments retain the source ID even when the drawable is a placeholder.
        self.assertEqual(output, generate(index, {'ic_lib_old': xml}, output[MAP].decode(), 'a' * 40))
        with self.assertRaises(ValueError):
            generate(index, {}, previous, 'a' * 40)
        with self.assertRaises(ValueError):
            generate(index, {'ic_lib_old': b'<!DOCTYPE vector><vector/>'}, previous, 'a' * 40)
        index['0']['iconId'] = 'ic_lib_reassigned'
        with self.assertRaises(ValueError):
            generate(index, {'ic_lib_old': xml}, previous, 'a' * 40)


if __name__ == '__main__':
    unittest.main()
