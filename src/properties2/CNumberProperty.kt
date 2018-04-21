package properties2

import javafx.beans.property.Property
import ui.CustomSlider

/**
 * 数値を持つプロパティの親クラス
 * スライダーUIをもつ
 */
abstract class CNumberProperty(override val valueProperty: Property<Number>,var min : Number,var max : Number,var def : Number,var tick : Number) : CitrusProperty<Number> {
    private val slider = CustomSlider()

    override val uiNode: CustomSlider
        get() = slider

    //TODO プロパティ側の max min をUIに反映させる
    init{
        uiNode.tick = tick.toDouble()
        uiNode.min = min.toDouble()
        uiNode.max = max.toDouble()
        uiNode.value = def.toDouble()
        value = def
    }
}