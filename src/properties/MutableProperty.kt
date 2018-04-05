package properties

import interpolation.Interpolator
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import kotlin.math.absoluteValue

/**
 * キーフレームを持つプロパティ
 */
class MutableProperty(var min: Double = -1000.0, var max: Double = 1000.0, var prefMin: Double = 100.0, var prefMax: Double = 100.0, def : Double = 0.0) {
    /**
     * キーフレームのデータクラス
     */
    data class KeyFrame(var interpolation: Interpolator, var frame: IntegerProperty = SimpleIntegerProperty(), var value: DoubleProperty = SimpleDoubleProperty())

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
                keyFrames[0].value.value
            else {
                val index = getKeyFrameIndex(frame)
                if (index == keyFrames.size - 1)//最後のキーフレームの場合
                {
                    keyFrames[index].value.value
                }else if(index == -1)//初めのキーフレームに到達してない場合
                {
                    keyFrames[0].value.value
                }
                else {
                    val x = (frame - keyFrames[index].frame.value).toDouble() / (keyFrames[index + 1].frame.value - keyFrames[index].frame.value)
                    keyFrames[index].value.value + (keyFrames[index].interpolation.getInterpolation(x) * (keyFrames[index + 1].value.value - keyFrames[index].value.value))
                }
            }

    }

    /**
     * 指定されたフレームが何番目のキーフレームの直後にあるかを算出
     */
    fun getKeyFrameIndex(frame: Int): Int {
        for ((i, k) in keyFrames.withIndex()) {
            //初めてフレーム番号を越した場合、それが手前のキーフレームになる
            if (frame < k.frame.value) {
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
            if (k.frame.value == frame)
                return i

        return -1
    }
}