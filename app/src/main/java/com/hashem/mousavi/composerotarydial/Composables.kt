@file:OptIn(ExperimentalTextApi::class)

package com.hashem.mousavi.composerotarydial

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun RotaryDial(
    modifier: Modifier,
    innerCircleRadius: Dp = 70.dp,
    onNumberDialed: (Int) -> Unit
) {
    val angle = remember { (-360f / 11).toRadian() }
    val limitAngle = remember { angle * 10 }

    val angleForNumbersMap = remember {
        mutableMapOf<Int, Float>().apply {
            for (number in 0..9) {
                val radian = if (number > 0) (number - 1) * angle else 9 * angle
                this[number] = radian
            }
        }
    }

    var width by remember {
        mutableStateOf(0f)
    }
    var height by remember {
        mutableStateOf(0f)
    }

    var ringWidth by remember {
        mutableStateOf(0f)
    }

    var radius by remember {
        mutableStateOf((width - ringWidth) / 2)
    }

    val textMeasure = rememberTextMeasurer()
    var dragAngle by remember {
        mutableStateOf(0f)
    }
    var middle by remember {
        mutableStateOf(Offset.Zero)
    }

    val animatable = remember {
        Animatable(initialValue = 1f)
    }

    val scope = rememberCoroutineScope()

    var draggingNumber by remember {
        mutableStateOf(-1)
    }

    var isReachedToTheLimit by remember {
        mutableStateOf(false)
    }

    val circleRadius = with(LocalDensity.current) { 30.dp.toPx() }


    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(true) {
                    detectDragGestures(
                        onDragStart = {
                            //check if inside any circle clicked, if so, set draggingNumber
                            for (number in 0..9) {
                                val radian = angleForNumbersMap[number] ?: 0f
                                val centerOfCircle =
                                    Offset(
                                        x = radius * cos(radian) + width / 2,
                                        y = radius * sin(radian) + height / 2
                                    )

                                if (centerOfCircle.distance(it) <= circleRadius) {
                                    scope.launch {
                                        animatable.snapTo(1f)
                                        draggingNumber = number
                                    }
                                    break
                                }

                            }

                        },
                        onDragEnd = {
                            //rotate dial pad back to the original state
                            scope.launch {
                                val duration = 500
                                animatable.animateTo(
                                    0f,
                                    animationSpec = tween(
                                        durationMillis = duration,
                                        easing = LinearEasing
                                    )
                                )
                                dragAngle = 0f
                                isReachedToTheLimit = false
                                draggingNumber = -1
                            }
                        }
                    ) { change, _ ->
                        if (draggingNumber == -1 || isReachedToTheLimit) return@detectDragGestures

                        val x2 = change.position.x - middle.x
                        val y2 = middle.y - change.position.y

                        val angle2 = atan(y2 / x2).run {
                            if (x2 > 0) (this - PI / 2).toFloat()
                            else if (x2 < 0) (this - PI * 1.5).toFloat()
                            else this
                        }


                        val x1 = change.previousPosition.x - middle.x
                        val y1 = middle.y - change.previousPosition.y

                        val angle1 = atan(y1 / x1).run {
                            if (x1 > 0) (this - PI / 2).toFloat()
                            else if (x1 < 0) (this - PI * 1.5).toFloat()
                            else this
                        }

                        var angleChange = angle2 - angle1

                        if (angleChange > 350f.toRadian()) {
                            angleChange += (-360f).toRadian()
                        } else if (angleChange < (-350f).toRadian()) {
                            angleChange += 360f.toRadian()
                        }

                        angleForNumbersMap[draggingNumber]?.let { angleForNumber ->

                            val alpha1 =
                                (angleForNumber - dragAngle - angleChange)
                                    .toDegree()
                                    .roundToLong()
                            val alpha2 = (limitAngle)
                                .toDegree()
                                .roundToLong()

                            if (alpha1 - alpha2 >= 360 || alpha1 - alpha2 < 0) {
                                isReachedToTheLimit = true
                                dragAngle = angleForNumber + angle
                                onNumberDialed(draggingNumber)
                                return@let
                            }

                            dragAngle += angleChange

                        }

                    }
                }
        ) {
            width = this.size.width
            height = this.size.height

            middle = Offset(width / 2, height / 2)

            ringWidth = width / 2 - innerCircleRadius.toPx()
            radius = (width - ringWidth) / 2

            translate(left = width / 2, top = height / 2) {

                //draw limiter
                val centerOfLimitCircle =
                    Offset(
                        x = radius * cos(limitAngle),
                        y = radius * sin(limitAngle)
                    )
                drawCircle(
                    color = Color.Red,
                    radius = circleRadius / 4f,
                    center = centerOfLimitCircle
                )

                //draw numbers
                for (number in 0..9) {
                    val text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color.Black,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(number.toString())
                        }
                    }

                    val textLayoutResult = textMeasure.measure(text)

                    val radian = angleForNumbersMap[number] ?: 0f

                    if (draggingNumber == number) {
                        drawCircle(
                            color = Color.Green.copy(alpha = 0.4f),
                            radius = circleRadius,
                            center = Offset(
                                x = radius * cos(radian - dragAngle * animatable.value),
                                y = radius * sin(radian - dragAngle * animatable.value)
                            ),
                            style = Stroke(width = 5.dp.toPx())
                        )
                    } else if (draggingNumber != -1) {
                        val center = Offset(
                            x = radius * cos(radian - dragAngle * animatable.value),
                            y = radius * sin(radian - dragAngle * animatable.value)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color.Transparent, Color.Black),
                                center = center,
                                radius = circleRadius * 2
                            ),
                            radius = circleRadius,
                            center = center
                        )
                    }

                    val x = radius * cos(radian)
                    val y = radius * sin(radian)
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = x - textLayoutResult.size.width / 2f,
                            y = y - textLayoutResult.size.height / 2f
                        )
                    )
                }

                clipPath(
                    path = Path().apply {
                        for (num in 0..9) {
                            var radian = angleForNumbersMap[num] ?: 0f

                            radian -= dragAngle * animatable.value

                            val x = radius * cos(radian)
                            val y = radius * sin(radian)

                            addOval(
                                Rect(
                                    topLeft = Offset(
                                        x = x - circleRadius,
                                        y = y - circleRadius
                                    ),
                                    bottomRight = Offset(
                                        x = x + circleRadius,
                                        y = y + circleRadius
                                    )
                                )
                            )
                        }
                    },
                    clipOp = ClipOp.Difference
                ) {
                    drawCircle(
                        color = Color.Blue.copy(alpha = 0.3f),
                        radius = width / 2,
                        center = Offset.Zero
                    )

                }
            }

        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(innerCircleRadius * 2)
                .background(Color.Green, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = isReachedToTheLimit) {
                if (it) {
                    if (draggingNumber != -1) {
                        Text(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentHeight(),
                            text = draggingNumber.toString(),
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(30.dp),
                        painter = painterResource(id = R.drawable.ic_baseline_local_phone_24),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(color = Color.White)
                    )
                }
            }
        }


    }

}

private fun Float.toDegree(): Float = (this * 180f / PI).toFloat()

private fun Float.toRadian(): Float = (this * PI / 180f).toFloat()

private fun Offset.distance(other: Offset): Float {
    return sqrt((x - other.x).pow(2) + (y - other.y).pow(2))
}


@Preview
@Composable
fun RotaryDialPreview() {
    RotaryDial(
        modifier = Modifier.size(400.dp),
        onNumberDialed = {}
    )
}

