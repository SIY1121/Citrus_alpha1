package properties

import interpolation.Interpolator

/**
 * キーフレームを持つプロパティ
 */
class MutableProperty(var min: Double = Double.NEGATIVE_INFINITY, var max: Double = Double.POSITIVE_INFINITY, var prefMin: Double = 100.0, var prefMax: Double = 100.0,var tick : Double = 1.0 ,def : Double = 0.0) {
    /**
     * キーフレームのデータクラス
     */
    data class KeyFrame(var frame: Int, var interpolation: Interpolator, var value: Double)

    val keyFrames: MutableList<KeyFrame> = ArrayList()


    /**
     * キーフレームが無い時用の値
     */
    var fixedValue = 0.0

    /**
     * 値変更によるプレビュー表示用
     */
    var temporaryValue = 0.0
    var temporaryMode = false

    init {
        fixedValue = def
    }

    /**
     * 指定されたフレーム時点での値を取得
     */
    fun value(frame: Int): Double {
        return if (keyFrames.size == 0)
            fixedValue
        else if (temporaryMode)
        //値を変更している場合の一時的な表示用
            temporaryValue
        else
            if (keyFrames.size == 1)
                keyFrames[0].value
            else {
                val index = getKeyFrameIndex(frame)
                if (index == keyFrames.size - 1)//最後のキーフレームの場合
                {
                    keyFrames[index].value
                }else if(index == -1)//初めのキーフレームに到達してない場合
                {
                    keyFrames[0].value
                }
                else {
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
    fun isKeyFrame(frame: Int): Int {
        for ((i, k) in keyFrames.withIndex())
            if (k.frame == frame)
                return i

        return -1
    }
}