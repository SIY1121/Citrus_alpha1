package objects

import interpolation.Interpolation
import interpolation.LinearInterpolation

/**
 * キーフレームを持つプロパティ
 */
class MutableProperty {
    /**
     * キーフレームのデータクラス
     */
    data class KeyFrame(var frame: Int, var interpolation: Interpolation, var value: Double)

    val keyFrames: MutableList<KeyFrame> = ArrayList()

    var min = -1000.0
    var max = 1000.0
    var prefMin = -100.0
    var prefMax = 100.0


    init {
        keyFrames.add(KeyFrame(0, LinearInterpolation(), 0.0))
        //keyFrames.add(KeyFrame(Int.MAX_VALUE, LinearInterpolation(), 0.0))//TODO 最後のキーフレームを、オブジェクトの長さに追従させる
    }

    /**
     * 指定されたフレーム時点での値を取得
     */
    fun value(frame: Int): Double {
        return if(keyFrames.size==1)
            keyFrames[0].value
        else{
            val index = getKeyFrameIndex(frame)
            val x = (frame - keyFrames[index].frame).toDouble() / (keyFrames[index + 1].frame - keyFrames[index].frame)
            keyFrames[index].value + (keyFrames[index].interpolation.getInterpolation(x) * (keyFrames[index + 1].value - keyFrames[index].value))
        }

    }

    /**
     * 指定されたフレームが何番目のキーフレームの直後にあるかを算出
     */
    private fun getKeyFrameIndex(frame: Int): Int {
        for ((i, k) in keyFrames.withIndex()) {
            //初めてフレーム番号を越した場合、それが手前のキーフレームになる
            if (k.frame <= frame)
                return i
        }
        throw IndexOutOfBoundsException("指定されたフレームは範囲外です")
    }
}