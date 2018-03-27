package objects

import interpolation.Interpolation

/**
 * キーフレームを持つプロパティ
 */
class MutableProperty {

    /**
     * キーフレームのデータクラス
     */
    data class KeyFrame(var frame: Int, var interpolation: Interpolation, var value: Float)

    val keyFrames: MutableList<KeyFrame> = ArrayList()

    /**
     * 指定されたフレーム時点での値を取得
     */
    fun value(frame: Int): Float {
        val index = getKeyFrameIndex(frame)
        val x = (frame - keyFrames[index].frame).toFloat() / (keyFrames[index + 1].frame - keyFrames[index].frame)
        return keyFrames[index].value + (keyFrames[index].interpolation.getInterpolation(x) * (keyFrames[index + 1].value - keyFrames[index].value))
    }

    /**
     * 指定されたフレームが何番目のキーフレームの直後にあるかを算出
     */
    private fun getKeyFrameIndex(frame: Int): Int {
        for ((i, k) in keyFrames.withIndex()) {
            //初めてフレーム番号を越した場合、それが手前のキーフレームになる
            if (k.frame < frame)
                return i
        }
        throw IndexOutOfBoundsException("指定されたフレームは範囲外です")
    }
}