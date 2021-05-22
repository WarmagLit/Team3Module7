package com.example.graphiceditor

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.provider.CalendarContract
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.example.graphiceditor.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Box : View {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val backgroundColor = R.color.colorBackground

    private var nodeColor = Color.BLUE
    private var edgeColor = Color.BLACK
    private var nodeSize = 12

    // Координаты для поворота кубика
    var oldx: Float = 0.0f
    var oldy: Float = 0.0f

    var nodes = arrayOf(
        Koord(-250.0, -250.0, -250.0),
        Koord(-250.0, -250.0, 250.0),
        Koord(-250.0, 250.0, -250.0),
        Koord(-250.0, 250.0, 250.0),
        Koord(250.0, -250.0, -250.0),
        Koord(250.0, -250.0, 250.0),
        Koord(250.0, 250.0, -250.0),
        Koord(250.0, 250.0, 250.0)
    )


    private var edges = arrayOf(
        arrayOf(0, 1),
        arrayOf(1, 3),
        arrayOf(3, 2),
        arrayOf(2, 0),
        arrayOf(4, 5),
        arrayOf(5, 7),
        arrayOf(7, 6),
        arrayOf(6, 4),
        arrayOf(0, 4),
        arrayOf(1, 5),
        arrayOf(2, 6),
        arrayOf(3, 7)
    )

    var numbers = arrayOf(
        //Один
        Koord(0.0, 0.0, 250.0),

        Koord(0.0, 150.0, 250.0),
        Koord(0.0, -150.0, 250.0),

        //Два
        Koord(0.0, 0.0, -250.0),

        Koord(-50.0, 150.0, -250.0),
        Koord(-50.0, -150.0, -250.0),
        Koord(50.0, 150.0, -250.0),
        Koord(50.0, -150.0, -250.0),

        //Три
        Koord(0.0, 250.0, 0.0),

        Koord(-50.0, 250.0, -150.0),
        Koord(-50.0, 250.0, 150.0),
        Koord(0.0, 250.0, -150.0),
        Koord(0.0, 250.0, 150.0),
        Koord(50.0, 250.0, -150.0),
        Koord(50.0, 250.0, 150.0),

        //Четыре
        Koord(0.0, -250.0, 0.0),

        Koord(-50.0, -250.0, -150.0),
        Koord(-50.0, -250.0, 150.0),
        Koord(0.0, -250.0, -150.0),
        Koord(50.0, -250.0, 150.0),
        Koord(50.0, -250.0, 150.0),
        Koord(100.0, -250.0, -150.0),

        // Пять
        Koord(250.0, 0.0, 0.0),

        Koord(250.0, -150.0, 50.0),
        Koord(250.0, 150.0, 0.0),
        Koord(250.0, 150.0, 0.0),
        Koord(250.0, -150.0, -50.0),


        //Шесть
        Koord(-250.0, 0.0, 0.0),

        Koord(-250.0, -150.0, -100.0),
        Koord(-250.0, 150.0, -50.0),
        Koord(-250.0, 150.0, -50.0),
        Koord(-250.0, -150.0, 0.0),
        Koord(-250.0, -150.0, 50.0),
        Koord(-250.0, 150.0, 50.0)
    )

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                oldx = event.x
                oldy = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                rotateY3D((event.x - oldx) / 200)
                rotateX3D((event.y - oldy) / 200)
                oldx = event.x
                oldy = event.y
            }
        }
        invalidate()
        return true
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(backgroundColor)
        refreshCanvas(canvas)
        rotateZ3D(0.0)
    }


    private fun rotateX3D(theta: Float) {
        val sinTheta = sin(theta)
        val cosTheta = cos(theta)
        for (n in nodes.indices) {
            val node = nodes[n]
            val y = node.y
            val z = node.z
            node.y = (y * cosTheta - z * sinTheta)
            node.z = (z * cosTheta + y * sinTheta)
            nodes[n] = node
        }


        for (n in numbers.indices) {
            val num = numbers[n]
            val y = num.y
            val z = num.z
            num.y = (y * cosTheta - z * sinTheta)
            num.z = (z * cosTheta + y * sinTheta)
            numbers[n] = num
        }
    }

    fun rotateY3D(theta: Float) {
        val sinTheta = sin(theta)
        val cosTheta = cos(theta)
        for (n in nodes.indices) {
            val node = nodes[n]
            val x = node.x
            val z = node.z
            node.x = (x * cosTheta + z * sinTheta)
            node.z = (z * cosTheta - x * sinTheta)
            nodes[n] = node
        }


        for (n in numbers.indices) {
            val num = numbers[n]
            val x = num.x
            val z = num.z
            num.x = (x * cosTheta + z * sinTheta)
            num.z = (z * cosTheta - x * sinTheta)
            numbers[n] = num
        }
    }

    fun rotateZ3D(theta: Double) {
        val sinTheta = sin(theta)
        val cosTheta = cos(theta)

        for (n in nodes.indices) {
            val node = nodes[n]
            val x = node.x
            val y = node.y
            node.x = (x * cosTheta + y * sinTheta)
            node.y = (y * cosTheta - x * sinTheta)
            nodes[n] = node
        }

        for (n in numbers.indices) {
            val num = numbers[n]
            val x = num.x
            val y = num.y
            num.x = (x * cosTheta + y * sinTheta)
            num.y = (y * cosTheta - x * sinTheta)
            numbers[n] = num
        }
    }


    fun refreshCanvas(canvas: Canvas) {
        canvas.translate((canvas.width / 2).toFloat(), (canvas.height / 2).toFloat())
        val mPath = Path()
        val mPaint = Paint()
        val colorPath = Path()
        val colorPaint = Paint()

        if (numbers[0].z > 0.0) {
            firstColor(colorPath, canvas, colorPaint)
            one(mPath, canvas, mPaint)
        }
        if (numbers[3].z > 0.0) {
            secondColor(colorPath, canvas, colorPaint)
            two(mPath, canvas, mPaint)
        }
        if (numbers[8].z > 0.0) {
            thirdColor(colorPath, canvas, colorPaint)
            third(mPath, canvas, mPaint)
        }
        if (numbers[15].z > 0.0) {
            fourColor(colorPath, canvas, colorPaint)
            four(mPath, canvas, mPaint)
        }
        if (numbers[22].z > 0.0) {
            fiveColor(colorPath, canvas, colorPaint)
            five(mPath, canvas, mPaint)
        }
        if (numbers[27].z > 0.0) {
            sixColor(colorPath, canvas, colorPaint)
            six(mPath, canvas, mPaint)
        }


        var min = 1000.0
        paint.color = edgeColor
        paint.strokeWidth = 5f

        for (n in nodes.indices) {
            if (nodes[n].z < min)
                min = nodes[n].z
        }
        for (n in edges.indices) {
            val node0 = nodes[edges[n][0]]
            val node1 = nodes[edges[n][1]]
            if (node0.z != min && node1.z != min)
                canvas.drawLine(
                    node0.x.toFloat(),
                    node0.y.toFloat(),
                    node1.x.toFloat(),
                    node1.y.toFloat(),
                    paint
                )
        }

        paint.color = nodeColor
        for (element in nodes) {
            if (element.z != min)
                canvas.drawCircle(
                    element.x.toFloat(),
                    element.y.toFloat(), nodeSize.toFloat(), paint
                )
        }

    }


    private fun one(mPath: Path, canvas: Canvas, mPaint: Paint) {
        mPaint.color = Color.BLACK
        mPaint.strokeWidth = 15f
        mPaint.style = Paint.Style.STROKE
        mPath.moveTo(numbers[1].x.toFloat(), numbers[1].y.toFloat())
        mPath.lineTo(numbers[2].x.toFloat(), numbers[2].y.toFloat())
        canvas.drawPath(mPath, mPaint)
        mPath.reset()
    }

    private fun two(mPath: Path, canvas: Canvas, mPaint: Paint) {
        mPaint.strokeWidth = 15f
        mPaint.style = Paint.Style.STROKE
        mPath.moveTo(numbers[4].x.toFloat(), numbers[4].y.toFloat())
        mPath.lineTo(numbers[5].x.toFloat(), numbers[5].y.toFloat())
        mPath.moveTo(numbers[6].x.toFloat(), numbers[6].y.toFloat())
        mPath.lineTo(numbers[7].x.toFloat(), numbers[7].y.toFloat())
        canvas.drawPath(mPath, mPaint)
        mPath.reset()
    }

    private fun third(mPath: Path, canvas: Canvas, mPaint: Paint) {
        mPaint.strokeWidth = 15f
        mPaint.style = Paint.Style.STROKE
        mPath.moveTo(numbers[9].x.toFloat(), numbers[9].y.toFloat())
        mPath.lineTo(numbers[10].x.toFloat(), numbers[10].y.toFloat())
        mPath.moveTo(numbers[11].x.toFloat(), numbers[11].y.toFloat())
        mPath.lineTo(numbers[12].x.toFloat(), numbers[12].y.toFloat())
        mPath.moveTo(numbers[13].x.toFloat(), numbers[13].y.toFloat())
        mPath.lineTo(numbers[14].x.toFloat(), numbers[14].y.toFloat())
        canvas.drawPath(mPath, mPaint)
        mPath.reset()
    }

    private fun four(mPath: Path, canvas: Canvas, mPaint: Paint) {
        mPaint.strokeWidth = 15f
        mPaint.style = Paint.Style.STROKE
        mPath.moveTo(numbers[16].x.toFloat(), numbers[16].y.toFloat())
        mPath.lineTo(numbers[17].x.toFloat(), numbers[17].y.toFloat())
        mPath.moveTo(numbers[18].x.toFloat(), numbers[18].y.toFloat())
        mPath.lineTo(numbers[19].x.toFloat(), numbers[19].y.toFloat())
        mPath.moveTo(numbers[20].x.toFloat(), numbers[20].y.toFloat())
        mPath.lineTo(numbers[21].x.toFloat(), numbers[21].y.toFloat())
        canvas.drawPath(mPath, mPaint)
        mPath.reset()
    }

    private fun five(mPath: Path, canvas: Canvas, mPaint: Paint) {
        mPaint.strokeWidth = 15f
        mPaint.style = Paint.Style.STROKE
        mPath.moveTo(numbers[23].x.toFloat(), numbers[23].y.toFloat())
        mPath.lineTo(numbers[24].x.toFloat(), numbers[24].y.toFloat())
        mPath.moveTo(numbers[25].x.toFloat(), numbers[25].y.toFloat())
        mPath.lineTo(numbers[26].x.toFloat(), numbers[26].y.toFloat())
        canvas.drawPath(mPath, mPaint)
        mPath.reset()
    }

    private fun six(mPath: Path, canvas: Canvas, mPaint: Paint) {
        mPaint.strokeWidth = 15f
        mPaint.style = Paint.Style.STROKE
        mPath.moveTo(numbers[28].x.toFloat(), numbers[28].y.toFloat())
        mPath.lineTo(numbers[29].x.toFloat(), numbers[29].y.toFloat())
        mPath.moveTo(numbers[30].x.toFloat(), numbers[30].y.toFloat())
        mPath.lineTo(numbers[31].x.toFloat(), numbers[31].y.toFloat())
        mPath.moveTo(numbers[32].x.toFloat(), numbers[32].y.toFloat())
        mPath.lineTo(numbers[33].x.toFloat(), numbers[33].y.toFloat())
        canvas.drawPath(mPath, mPaint)
        mPath.reset()
    }

    private fun firstColor(colorPath: Path, canvas: Canvas, colorPaint: Paint) {
        colorPaint.style = Paint.Style.FILL
        colorPaint.color = Color.GREEN
        colorPath.moveTo(nodes[3].x.toFloat(), nodes[3].y.toFloat())
        colorPath.lineTo(nodes[1].x.toFloat(), nodes[1].y.toFloat())
        colorPath.lineTo(nodes[5].x.toFloat(), nodes[5].y.toFloat())
        colorPath.lineTo(nodes[7].x.toFloat(), nodes[7].y.toFloat())

        canvas.drawPath(colorPath, colorPaint)
        colorPath.reset()
    }

    private fun secondColor(colorPath: Path, canvas: Canvas, colorPaint: Paint) {
        colorPaint.color = Color.RED
        colorPath.moveTo(nodes[0].x.toFloat(), nodes[0].y.toFloat())
        colorPath.lineTo(nodes[2].x.toFloat(), nodes[2].y.toFloat())
        colorPath.lineTo(nodes[6].x.toFloat(), nodes[6].y.toFloat())
        colorPath.lineTo(nodes[4].x.toFloat(), nodes[4].y.toFloat())
        canvas.drawPath(colorPath, colorPaint)
        colorPath.reset()
    }

    private fun thirdColor(colorPath: Path, canvas: Canvas, colorPaint: Paint) {
        colorPaint.color = Color.YELLOW
        colorPath.moveTo(nodes[3].x.toFloat(), nodes[3].y.toFloat())
        colorPath.lineTo(nodes[7].x.toFloat(), nodes[7].y.toFloat())
        colorPath.lineTo(nodes[6].x.toFloat(), nodes[6].y.toFloat())
        colorPath.lineTo(nodes[2].x.toFloat(), nodes[2].y.toFloat())
        canvas.drawPath(colorPath, colorPaint)
        colorPath.reset()
    }

    private fun fourColor(colorPath: Path, canvas: Canvas, colorPaint: Paint) {
        colorPaint.color = Color.GRAY
        colorPath.moveTo(nodes[5].x.toFloat(), nodes[5].y.toFloat())
        colorPath.lineTo(nodes[4].x.toFloat(), nodes[4].y.toFloat())
        colorPath.lineTo(nodes[0].x.toFloat(), nodes[0].y.toFloat())
        colorPath.lineTo(nodes[1].x.toFloat(), nodes[1].y.toFloat())
        canvas.drawPath(colorPath, colorPaint)
        colorPath.reset()
    }

    private fun fiveColor(colorPath: Path, canvas: Canvas, colorPaint: Paint) {
        colorPaint.color = Color.BLUE
        colorPath.moveTo(nodes[5].x.toFloat(), nodes[5].y.toFloat())
        colorPath.lineTo(nodes[7].x.toFloat(), nodes[7].y.toFloat())
        colorPath.lineTo(nodes[6].x.toFloat(), nodes[6].y.toFloat())
        colorPath.lineTo(nodes[4].x.toFloat(), nodes[4].y.toFloat())
        canvas.drawPath(colorPath, colorPaint)
        colorPath.reset()
    }

    private fun sixColor(colorPath: Path, canvas: Canvas, colorPaint: Paint) {
        colorPaint.color = Color.MAGENTA
        colorPath.moveTo(nodes[1].x.toFloat(), nodes[1].y.toFloat())
        colorPath.lineTo(nodes[3].x.toFloat(), nodes[3].y.toFloat())
        colorPath.lineTo(nodes[2].x.toFloat(), nodes[2].y.toFloat())
        colorPath.lineTo(nodes[0].x.toFloat(), nodes[0].y.toFloat())
        canvas.drawPath(colorPath, colorPaint)
        colorPath.reset()
    }

}

