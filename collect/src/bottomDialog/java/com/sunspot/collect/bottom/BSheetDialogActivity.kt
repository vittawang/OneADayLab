package com.sunspot.collect.bottom

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sunspot.collect.R
import com.sunspot.collect.databinding.ActivityBottomSheetDialogBinding
import com.sunspot.libext.addAndShow

class BSheetDialogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityBottomSheetDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.apply {
            btnFixHeight480.setOnClickListener {
                BFixHeight480DialogFragment().addAndShow(supportFragmentManager)
            }

            btnFixHei2.setOnClickListener {
                BFixHeight480DialogFragment2().addAndShow(
                    supportFragmentManager
                )
            }

            btnWrapHeight.setOnClickListener {
                BWrapHeightDialogFragment.newInstance(
                    "第一步：重点松解紧张的右侧（治本）\n" +
                            "拉伸右侧斜方肌上束：坐直，用左手将头轻柔拉向左前方，同时右肩有意识下沉。感受右侧颈肩部的拉伸。保持30秒，重复3次。\n" +
                            "\n" +
                            "筋膜球松解右侧肩胛提肌：靠墙，将筋膜球或网球放在右侧脖子根部与肩胛骨上角之间（一个非常酸的点），靠身体重量轻轻滚动按压，持续30秒。这里通常是“重灾区”。\n" +
                            "\n" +
                            "拉伸右侧胸肌：右利手者右侧胸肌更紧。利用门框拉伸右侧胸部（方法如前所述）。\n" +
                            "\n" +
                            "第二步：重新训练左侧的发力模式（治标）\n" +
                            "目标不是“上提”左侧，而是 “让左侧肩胛骨向后、向下平贴回胸廓”。\n" +
                            "\n"
                ).addAndShow(supportFragmentManager)
            }
            btnW1.setOnClickListener {
                BWrapHeightDialogFragment.newInstance(
                    "第一步：重点松解紧张的右侧（治本）\n" +
                            "拉伸右侧斜方肌上束：坐直，用左手将头轻柔拉向左前方，同时右肩有意识下沉。感受右侧颈肩部的拉伸。保持30秒，重复3次。\n"
                ).addAndShow(supportFragmentManager)
            }
            btnW2.setOnClickListener {
                BWrapHeightDialogFragment.newInstance(
                    "明白了，您的情况是 “左肩高，右肩低”（左高右低），并且您是右利手。这是一个非常经典的组合，分析和对策会更有针对性。\n" +
                            "\n" +
                            "核心结论： 右利手却左肩高，这通常不是左边太用力，而是右边长期“工作模式”引发的一系列不对称代偿结果。\n" +
                            "\n" +
                            "为什么右利手反而左肩高？—— 原因分析\n" +
                            "右侧长期紧张下压（根源）：您长期用右手写字、用鼠标，右侧的斜方肌、肩胛提肌等处于一种 “持续性微紧张” 状态。这种紧张不仅会让右肩上提，更会让整个右侧肩胛带向前、向下旋转和内扣。时间久了，右侧的肌肉筋膜系统会把肩膀锁在一个“紧张性下沉”的位置。\n" +
                            "\n" +
                            "左侧的代偿性上提：为了平衡右侧这种向前向下的拉力，身体无意识地会让左侧肩膀向后、向上提来进行“对抗”，以维持视线的水平和身体的平衡。所以，您感觉左肩高，其实是身体为了“拉平”右侧的异常姿态而产生的代偿结果。\n" +
                            "\n" +
                            "脊柱侧弯的可能：长期的单侧不对称发力，可能已引发轻度的“C”型脊柱侧弯（胸椎段向右凸），为了保持头部中立，左侧肩膀必须抬高。您可以弯腰90度让家人从后方观察背部，看是否一侧更高。\n" +
                            "\n" +
                            "针对性调整策略：松解右侧 + 重置左侧\n" +
                            "您的策略需要从 “使劲抬左边” 彻底转变为 “放松拉回右边，并教会左边正确归位”。\n" +
                            "\n" +
                            "第一步：重点松解紧张的右侧（治本）\n" +
                            "拉伸右侧斜方肌上束：坐直，用左手将头轻柔拉向左前方，同时右肩有意识下沉。感受右侧颈肩部的拉伸。保持30秒，重复3次。\n" +
                            "\n" +
                            "筋膜球松解右侧肩胛提肌：靠墙，将筋膜球或网球放在右侧脖子根部与肩胛骨上角之间（一个非常酸的点），靠身体重量轻轻滚动按压，持续30秒。这里通常是“重灾区”。\n" +
                            "\n" +
                            "拉伸右侧胸肌：右利手者右侧胸肌更紧。利用门框拉伸右侧胸部（方法如前所述）。\n" +
                            "\n" +
                            "第二步：重新训练左侧的发力模式（治标）\n" +
                            "目标不是“上提”左侧，而是 “让左侧肩胛骨向后、向下平贴回胸廓”。\n" +
                            "\n" +
                            "左侧单侧“T”字练习：俯卧或站立，将左手向左侧水平伸出，拇指朝天。集中意念，用左侧后背的肌肉（而不是脖子或肩膀上方）发力，将左手和左侧肩胛骨向身体中线方向“收回来”，感受左侧后背中间的挤压感。做15次/组，3组。\n" +
                            "\n" +
                            "左侧靠墙天使：侧身站在墙边，左侧身体贴墙，左手做“投降-上举-下放”的靠墙天使动作，确保左侧肩胛骨始终平贴墙壁。\n" +
                            "\n" +
                            "第三步：修正日常姿势（根本）\n" +
                            "工作台设置：将电脑屏幕稍稍向左移动一些（约10-15厘米），迫使您在操作鼠标时，身体需要微微向左转，这有助于平衡右侧的紧张。\n" +
                            "\n" +
                            "右手鼠标使用习惯：有意识地每隔半小时，检查并放松右侧肩膀，让它从“紧张性下沉”中解放出来，回到自然位置。\n" +
                            "\n" +
                            "背包习惯：坚决使用双肩包，并调整好背带。如果必须用单肩包，请背在左肩（低的一侧），利用背包的重量帮助左侧下沉。\n" +
                            "\n" +
                            "睡姿：避免长期向一侧睡。可以尝试平躺。如果侧睡，避免高枕头，可在两膝间夹一个枕头保持骨盆中立。\n" +
                            "\n" +
                            "一个关键的自测动作\n" +
                            "坐直，闭上眼睛，做三次深呼吸，然后完全放松。\n" +
                            "让家人从正后方观察，或自己用手触摸两侧肩峰（肩膀最高点）。哪边更高？ 这个放松状态下的高度差，才是您真实的结构性高低。您之前“使劲”的状态，是代偿后的结果。\n" +
                            "\n" +
                            "总结给您的行动清单：\n" +
                            "\n" +
                            "停止：刻意用力抬起或绷紧左侧肩膀。\n" +
                            "\n" +
                            "开始：重点放松和拉伸右侧的颈、肩、胸部。\n" +
                            "\n" +
                            "训练：进行单侧的左侧背部肌群强化（如左侧T字、划船）。\n" +
                            "\n" +
                            "调整：改变工作环境和习惯，给身体对称的刺激。\n" +
                            "\n" +
                            "请记住，您是在纠正一个长达十几年的错误模式，需要耐心。坚持正确的放松和训练2-4周，您会首先感觉到双侧肩膀的轻松感，随后才是外观上的改善。如果条件允许，咨询物理治疗师进行一次专业评估和指导，会是最高效的选择。"
                ).addAndShow(supportFragmentManager)
            }
        }
    }
}