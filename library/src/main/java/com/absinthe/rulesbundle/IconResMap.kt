package com.absinthe.rulesbundle

import android.util.SparseIntArray
import com.absinthe.lc.rulesbundle.R

object IconResMap {
  private val MAP = SparseIntArray(100)
  private val SINGLE_COLOR_ICON_SET: Set<Int>

  init {
    MAP.apply {
      put(-1, R.drawable.ic_sdk_placeholder)
      put(0, R.drawable.ic_lib_360)
      put(1, R.drawable.ic_lib_airbnb)
      put(2, R.drawable.ic_lib_ali_security)
      put(3, R.drawable.ic_lib_alibaba)
      put(4, R.drawable.ic_lib_alipay)
      put(5, R.drawable.ic_lib_aliyun)
      put(6, R.drawable.ic_lib_amap)
      put(7, R.drawable.ic_lib_android)
      put(8, R.drawable.ic_lib_appauth)
      put(9, R.drawable.ic_lib_baidu)
      put(10, R.drawable.ic_lib_bilibili)
      put(11, R.drawable.ic_lib_tencent_tds)
      put(12, R.drawable.ic_lib_bytedance)
      put(13, R.drawable.ic_lib_chromium)
      put(14, R.drawable.ic_lib_cmb)
      put(15, R.drawable.ic_lib_cpp)
      put(16, R.drawable.ic_lib_didi)
      put(17, R.drawable.ic_lib_evernote)
      put(18, R.drawable.ic_lib_meta)
      put(19, R.drawable.ic_lib_firebase)
      put(20, R.drawable.ic_lib_flutter)
      put(21, R.drawable.ic_lib_golang)
      put(22, R.drawable.ic_lib_google)
      put(23, R.drawable.ic_lib_google_analytics)
      put(24, R.drawable.ic_lib_google_arcore)
      put(25, R.drawable.ic_lib_hapjs)
      put(26, R.drawable.ic_lib_huawei)
      put(27, R.drawable.ic_lib_iqiyi)
      put(28, R.drawable.ic_lib_jetpack)
      put(29, R.drawable.ic_lib_jpush)
      put(30, R.drawable.ic_lib_jverification)
      put(31, R.drawable.ic_lib_kuaishou)
      put(32, R.drawable.ic_lib_lua)
      put(33, R.drawable.ic_lib_material)
      put(34, R.drawable.ic_lib_meizu)
      put(35, R.drawable.ic_lib_microsoft)
      put(36, R.drawable.ic_lib_netease)
      put(37, R.drawable.ic_lib_opencv)
      put(38, R.drawable.ic_lib_oppo)
      put(39, R.drawable.ic_lib_play_store)
      put(40, R.drawable.ic_lib_qiniu)
      put(41, R.drawable.ic_lib_react)
      put(42, R.drawable.ic_lib_realm)
      put(43, R.drawable.ic_lib_rongyun)
      put(44, R.drawable.ic_lib_sensors)
      put(45, R.drawable.ic_lib_shizuku)
      put(46, R.drawable.ic_lib_sqlite)
      put(47, R.drawable.ic_lib_square)
      put(48, R.drawable.ic_lib_tencent)
      put(49, R.drawable.ic_lib_tencent_ad)
      put(50, R.drawable.ic_lib_tencent_cloud)
      put(51, R.drawable.ic_lib_tencent_map)
      put(52, R.drawable.ic_lib_tensorflow)
      put(53, R.drawable.ic_lib_umeng)
      put(54, R.drawable.ic_lib_unionpay)
      put(55, R.drawable.ic_lib_unity)
      put(56, R.drawable.ic_lib_unreal_engine)
      put(57, R.drawable.ic_lib_vivo)
      put(58, R.drawable.ic_lib_webrtc)
      put(59, R.drawable.ic_lib_weibo)
      put(60, R.drawable.ic_lib_xamarin)
      put(61, R.drawable.ic_lib_xiaoai)
      put(62, R.drawable.ic_lib_xiaomi)
      put(63, R.drawable.ic_lib_xunfei)
      put(64, R.drawable.ic_lib_zhihu)
      put(65, R.drawable.ic_lib_kotlin)
      put(66, R.drawable.ic_lib_telegram)
      put(67, R.drawable.ic_lib_ffmpeg)
      put(68, R.drawable.ic_lib_vlc)
      put(69, R.drawable.ic_lib_paypal)
      put(70, R.drawable.ic_lib_qt)
      put(71, R.drawable.ic_lib_dataranger)
      put(72, R.drawable.ic_lib_rclone)
      put(73, R.drawable.ic_lib_sentry)
      put(74, R.drawable.ic_lib_jd)
      put(75, R.drawable.ic_lib_curl)
      put(76, R.drawable.ic_lib_opencc)
      put(77, R.drawable.ic_lib_zbar)
      put(78, R.drawable.ic_lib_opus)
      put(79, R.drawable.ic_lib_live2d)
      put(80, R.drawable.ic_lib_google_ml_kit)
      put(81, R.drawable.ic_lib_yandex_speech_kit)
      put(82, R.drawable.ic_lib_getui)
      put(83, R.drawable.ic_lib_dx_risk)
      put(84, R.drawable.ic_lib_clash)
      put(85, R.drawable.ic_lib_jetpack_compose)
      put(86, R.drawable.ic_lib_douyin)
      put(87, R.drawable.ic_lib_agora)
      put(88, R.drawable.ic_lib_hermes)
      put(89, R.drawable.ic_lib_netease_yunxin)
      put(90, R.drawable.ic_lib_applovin)
      put(91, R.drawable.ic_lib_godot)
      put(92, R.drawable.ic_lib_gamemaker)
      put(93, R.drawable.ic_lib_cmcc)
      put(94, R.drawable.ic_lib_mpaas)
      put(95, R.drawable.ic_lib_node_js)
      put(96, R.drawable.ic_lib_fresco)
      put(97, R.drawable.ic_lib_meituan)
      put(98, R.drawable.ic_lib_netease_shield)
      put(99, R.drawable.ic_lib_vorbis)
      put(100, R.drawable.ic_lib_mapbox)
      put(101, R.drawable.ic_lib_google_cardboard)
      put(102, R.drawable.ic_lib_ncnn)
      put(103, R.drawable.ic_lib_youdao)
      put(104, R.drawable.ic_lib_honor)
      put(105, R.drawable.ic_lib_rn_reanimated)
      put(106, R.drawable.ic_lib_meiqia)
      put(107, R.drawable.ic_lib_instabug)
      put(108, R.drawable.ic_lib_rust)
      put(109, R.drawable.ic_lib_volcengine)
      put(110, R.drawable.ic_lib_wwise)
      put(111, R.drawable.ic_lib_gio)
      put(112, R.drawable.ic_lib_mpv)
      put(113, R.drawable.ic_lib_isar)
      put(114, R.drawable.ic_lib_snapdragon)
      put(115, R.drawable.ic_lib_oracle)
      put(116, R.drawable.ic_lib_javet)
      put(117, R.drawable.ic_lib_openvpn)
      put(118, R.drawable.ic_lib_wireguard)
      put(119, R.drawable.ic_lib_rootbeer)
      put(120, R.drawable.ic_lib_xiaohongshu)
      put(121, R.drawable.ic_lib_megvii)
      put(122, R.drawable.ic_lib_folly)
      put(123, R.drawable.ic_lib_ironsource)
      put(124, R.drawable.ic_lib_android_ide)
      put(125, R.drawable.ic_lib_rnscreen)
      put(126, R.drawable.ic_lib_fcitx)
      put(127, R.drawable.ic_lib_tuanjie)
      put(128, R.drawable.ic_lib_tvm)
      put(129, R.drawable.ic_lib_singbox)
      put(130, R.drawable.ic_lib_google_mediapipe)
      put(131, R.drawable.ic_lib_tencent_tds)
      put(132, R.drawable.ic_lib_tencent_beacon)
      put(133, R.drawable.ic_lib_tencent_youtu)
      put(134, R.drawable.ic_lib_jna)
      put(135, R.drawable.ic_lib_maplibre_native)
      put(136, R.drawable.ic_lib_graphene_os)
      put(137, R.drawable.ic_lib_termux)
      put(138, R.drawable.ic_lib_rive)
      put(139, R.drawable.ic_lib_lynx)
      put(140, R.drawable.ic_lib_expo)
      put(141, R.drawable.ic_lib_cordova)
    }

    SINGLE_COLOR_ICON_SET = setOf(
      -1, 2, 3, 5, 6, 9, 10, 11, 13, 14, 15, 16, 17,
      25, 27, 30, 31, 38, 40, 43, 44, 47, 48, 51, 53, 56,
      59, 61, 63, 64, 66, 74, 76, 81, 82, 83, 84, 93,
      97, 100, 101, 103, 104, 108, 110, 111, 124, 136,
      138, 139, 140
    )
  }

  fun getResIndex(res: Int) = MAP.keyAt(MAP.indexOfValue(res))

  fun getIconRes(index: Int) = MAP.get(index, R.drawable.ic_sdk_placeholder)

  fun isSingleColorIcon(index: Int) = SINGLE_COLOR_ICON_SET.contains(index)
}
