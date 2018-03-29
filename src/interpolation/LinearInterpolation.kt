package interpolation

/**
 * 直線補間を行う
 */
class LinearInterpolation : Interpolation() {
    override fun getInterpolation(input: Double):Double  {
        return input
    }
}