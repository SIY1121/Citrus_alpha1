package objects

import interpolation.Interpolator
import interpolation.LinearInterpolator

/**
 * キーフレームを持つプロパティ
 */
class MutableProperty {
    /**
     * キーフレームのデータクラス
     */
    data class KeyFrame(var frame: Int, var interpolation: Interpolator, var value: Double)

    val keyFrames: MutableList<KeyFrame> = ArrayList()

    var min = -1000.0
    var max = 1000.0
    var prefMin = -100.0
    var prefMax = 100.0

    /**
     * 値変更によるプレビュー表示用
     */
    var temporaryValue = 0.0
    var temporaryMode = false

    init {
        keyFrames.add(KeyFrame(0, LinearInterpolator(), 0.0))
    }

    /**
     * 指定されたフレーム時点での値を取得
     */
    fun value(frame: Int): Double {
        //値を変更している場合の一時的な表示用
        return if (temporaryMode)
            temporaryValue
        else
            if (keyFrames.size == 1)
                keyFrames[0].value
            else {
                val index = getKeyFrameIndex(frame)
                if (index == keyFrames.size - 1)//最後のキーフレームの場合
                {
                    keyFrames[index].value
                } else {
                    val x = (frame - keyFrames[index].frame).toDouble() / (keyFrames[index + 1].frame - keyFrames[index].frame)
                    keyFrames[index].value + (keyFrames[index].interpolation.getInterpolation(x) * (keyFrames[index + 1].value - keyFrames[index].value))
                }
            }

    }

    /**
     * 指定されたフレームが何番目のキーフレームの直後にあるかを算出
     */
    fun getKeyFrameIndex(frame: Int): Int {
        for ((i, k) in keyFrames.withIndex()) {
            //初めてフレーム番号を越した場合、それが手前のキーフレームになる
            if (frame < k.frame) {
                //println(i - 1)
                return i - 1
            }
        }
        return keyFrames.size - 1
    }

    /**
     * 指定したフレームがキーフレームか判定
     * キーフレームの場合は何番目のキーフレームかを返す
     * それ以外は-1
     */
    fun isKeyFrame(frame: Int) : Int{
        for((i,k) in keyFrames.withIndex())
            if(k.frame==frame)
                return i

        return -1
    }
}