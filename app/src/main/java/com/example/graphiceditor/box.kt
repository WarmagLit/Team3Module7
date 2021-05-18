package com.example.graphiceditor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.example.graphiceditor.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class box : View {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val backgroundColor = Color.WHITE
    var nodeColor = Color.BLUE
    var edgeColor = Color.BLACK
    var nodeSize = 12


    var Button = findViewById<Button>(R.id.RotateZ)




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


    var edges = arrayOf(arrayOf(0, 1),
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
            Koord(0.0, 150.0, 250.0),
            Koord(0.0, -150.0, 250.0),

            //Два
            Koord(-50.0,150.0,-250.0),
            Koord(-50.0,-150.0,-250.0),
            Koord(50.0,150.0,-250.0),
            Koord(50.0,-150.0,-250.0),

            //Три
            Koord(-50.0,250.0,-150.0),
            Koord(-50.0,250.0,150.0),
            Koord(0.0,250.0,-150.0),
            Koord(0.0,250.0,150.0),
            Koord(50.0,250.0,-150.0),
            Koord(50.0,250.0,150.0),

            //Четыре
            Koord(-50.0,-250.0,-150.0),
            Koord(-50.0,-250.0,150.0),
            Koord(0.0,-250.0,150.0),
            Koord(50.0,-250.0,-150.0),
            Koord(50.0,-250.0,-150.0),
            Koord(100.0,-250.0,150.0),

            //Шесть
            Koord(-250.0,-150.0,50.0),
            Koord(-250.0,150.0,0.0),
            Koord(-250.0,150.0,0.0),
            Koord(-250.0,-150.0,-50.0),
            Koord(-250.0,150.0,100.0),
            Koord(-250.0,-150.0,100.0),

            // Пять
            Koord(250.0,-150.0,50.0),
            Koord(250.0,150.0,0.0),
            Koord(250.0,150.0,0.0),
            Koord(250.0,-150.0,-50.0)
    )

    var oldx: Float = 0.0f
    var oldy: Float = 0.0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action){
            MotionEvent.ACTION_DOWN -> {
                oldx = event.x
                oldy = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                rotateY3D((event.x - oldx)/200)
                rotateX3D((event.y - oldy)/200)
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
        updateCanvas(canvas)

    }


    fun rotateX3D(theta: Float) {
        val sinTheta = sin(theta)
        val cosTheta = cos(theta)
        for (n in 0..nodes.size - 1) {
            val node = nodes[n]
            val y = node.y
            val z = node.z
            node.y = (y * cosTheta - z * sinTheta)
            node.z = (z * cosTheta + y * sinTheta)
            nodes[n] = node
        }


        for (n in 0..numbers.size - 1) {
            val num = numbers[n]
            var y = num.y
            var z = num.z
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
        var sinTheta = sin(theta);
        var cosTheta = cos(theta);

        for (n in 0..nodes.size -1) {
            nodes[n].x = (nodes[n].x * cosTheta - nodes[n].y * sinTheta)
            nodes[n].y = (nodes[n].y * cosTheta + nodes[n].x * sinTheta)
        }
    }
    fun updateCanvas(canvas: Canvas)
    {
        canvas.translate((canvas.width/2).toFloat(), (canvas.height/2).toFloat())
        val mPath = Path()
        var mPaint = Paint()


        var min: Double = -1000.0
        paint.color = edgeColor
        paint.strokeWidth = 5f

        for (n in 0..nodes.size - 1) {
            if (nodes[n].z > min)
                min = nodes[n].z
        }

        for (n in 0..edges.size - 1) {
            val node0 = nodes[edges[n][0]]
            val node1 = nodes[edges[n][1]]
            if (node0.z != min && node1.z != min)
                canvas.drawLine(node0.x.toFloat(), node0.y.toFloat(), node1.x.toFloat(), node1.y.toFloat(), paint)
        }

        paint.color = nodeColor
        for (n in 0..nodes.size - 1) {
            val node = nodes[n]
            if (node.z != min)
                canvas.drawCircle(node.x.toFloat(), node.y.toFloat(), nodeSize.toFloat(), paint)
        }

        One(mPath,canvas, mPaint)
        Two(mPath,canvas, mPaint)
        Third(mPath,canvas, mPaint)
        Four(mPath,canvas, mPaint)
        Five(mPath,canvas, mPaint)
        Six(mPath,canvas, mPaint)
    }




    fun One(mPath: Path, canvas: Canvas, mPaint: Paint)
    {
        mPaint.color = Color.BLACK
        mPaint.strokeWidth = 15f
        mPaint.style = Paint.Style.STROKE
        mPath.moveTo(numbers[0].x.toFloat(), numbers[0].y.toFloat())
        mPath.lineTo(numbers[1].x.toFloat(), numbers[1].y.toFloat())
        canvas.drawPath(mPath, mPaint)
        mPath.reset()
    }
    fun Two(mPath: Path, canvas: Canvas, mPaint: Paint)
    {
        mPaint.strokeWidth = 15f
        mPaint.style = Paint.Style.STROKE
        mPath.moveTo(numbers[2].x.toFloat(), numbers[2].y.toFloat())
        mPath.lineTo(numbers[3].x.toFloat(), numbers[3].y.toFloat())
        mPath.moveTo(numbers[4].x.toFloat(), numbers[4].y.toFloat())
        mPath.lineTo(numbers[5].x.toFloat(), numbers[5].y.toFloat())
        canvas.drawPath(mPath, mPaint)
        mPath.reset()
    }
    fun Third(mPath: Path, canvas: Canvas, mPaint: Paint)
    {
        mPaint.strokeWidth = 15f
        mPaint.style = Paint.Style.STROKE
        mPath.moveTo(numbers[6].x.toFloat(), numbers[6].y.toFloat())
        mPath.lineTo(numbers[7].x.toFloat(), numbers[7].y.toFloat())
        mPath.moveTo(numbers[8].x.toFloat(), numbers[8].y.toFloat())
        mPath.lineTo(numbers[9].x.toFloat(), numbers[9].y.toFloat())
        mPath.moveTo(numbers[10].x.toFloat(), numbers[10].y.toFloat())
        mPath.lineTo(numbers[11].x.toFloat(), numbers[11].y.toFloat())
        canvas.drawPath(mPath, mPaint)
        mPath.reset()
    }
    fun Four(mPath: Path, canvas: Canvas, mPaint: Paint)
    {
        mPaint.strokeWidth = 15f
        mPaint.style = Paint.Style.STROKE
        mPath.moveTo(numbers[12].x.toFloat(), numbers[12].y.toFloat())
        mPath.lineTo(numbers[13].x.toFloat(), numbers[13].y.toFloat())
        mPath.moveTo(numbers[14].x.toFloat(), numbers[14].y.toFloat())
        mPath.lineTo(numbers[15].x.toFloat(), numbers[15].y.toFloat())
        mPath.moveTo(numbers[16].x.toFloat(), numbers[16].y.toFloat())
        mPath.lineTo(numbers[17].x.toFloat(), numbers[17].y.toFloat())
        canvas.drawPath(mPath, mPaint)
        mPath.reset()
    }
    fun Five(mPath: Path, canvas: Canvas, mPaint: Paint)
    {
        mPaint.strokeWidth = 15f
        mPaint.style = Paint.Style.STROKE
        mPath.moveTo(numbers[24].x.toFloat(), numbers[24].y.toFloat())
        mPath.lineTo(numbers[25].x.toFloat(), numbers[25].y.toFloat())
        mPath.moveTo(numbers[26].x.toFloat(), numbers[26].y.toFloat())
        mPath.lineTo(numbers[27].x.toFloat(), numbers[27].y.toFloat())
        canvas.drawPath(mPath, mPaint)
        mPath.reset()
    }
    fun Six(mPath: Path, canvas: Canvas, mPaint: Paint)
    {
        mPaint.strokeWidth = 15f
        mPaint.style = Paint.Style.STROKE
        mPath.moveTo(numbers[18].x.toFloat(), numbers[18].y.toFloat())
        mPath.lineTo(numbers[19].x.toFloat(), numbers[19].y.toFloat())
        mPath.moveTo(numbers[20].x.toFloat(), numbers[20].y.toFloat())
        mPath.lineTo(numbers[21].x.toFloat(), numbers[21].y.toFloat())
        mPath.moveTo(numbers[22].x.toFloat(), numbers[22].y.toFloat())
        mPath.lineTo(numbers[23].x.toFloat(), numbers[23].y.toFloat())
        canvas.drawPath(mPath, mPaint)
        mPath.reset()
    }
}

