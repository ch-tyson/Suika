package com.example.suika

import java.util.TimerTask

class GameTimerTask(private val activity: MainActivity) : TimerTask() {
    override fun run() {
        activity.updateModel()
        activity.updateView()

    }
}
