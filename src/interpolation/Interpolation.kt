package interpolation

/**
 * キーフレームの補完をする抽象クラス
 */
abstract class Interpolation {
    abstract fun getInterpolation(input : Double):Double
}