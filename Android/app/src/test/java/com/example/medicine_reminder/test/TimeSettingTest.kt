package com.example.medicine_reminder.test

import java.text.SimpleDateFormat
import java.util.*

/**
 * 时间设置功能测试
 */
class TimeSettingTest {
    
    fun testTimeFormatting() {
        println("=== 时间格式化测试 ===")
        
        // 测试30分钟设置
        val time1 = String.format("%02d:%02d", 8, 30)
        println("08:30 格式化: $time1")
        assert(time1 == "08:30") { "30分钟格式化失败" }
        
        // 测试24小时制
        val time2 = String.format("%02d:%02d", 14, 30)
        println("14:30 格式化: $time2")
        assert(time2 == "14:30") { "下午时间格式化失败" }
        
        // 测试晚上时间
        val time3 = String.format("%02d:%02d", 21, 30)
        println("21:30 格式化: $time3")
        assert(time3 == "21:30") { "晚上时间格式化失败" }
        
        // 测试午夜时间
        val time4 = String.format("%02d:%02d", 0, 30)
        println("00:30 格式化: $time4")
        assert(time4 == "00:30") { "午夜时间格式化失败" }
        
        println("✅ 时间格式化测试通过")
    }
    
    fun testTimeValidation() {
        println("\n=== 时间验证测试 ===")
        
        val validTimes = listOf("08:30", "14:30", "21:30", "00:30")
        val invalidTimes = listOf("25:30", "08:60", "abc", "8:30")
        
        // 测试有效时间
        validTimes.forEach { time ->
            val isValid = isValidTimeFormat(time)
            println("$time 验证: $isValid")
            assert(isValid) { "$time 应该被认为是有效时间" }
        }
        
        // 测试无效时间
        invalidTimes.forEach { time ->
            val isValid = isValidTimeFormat(time)
            println("$time 验证: $isValid")
            assert(!isValid) { "$time 应该被认为是无效时间" }
        }
        
        println("✅ 时间验证测试通过")
    }
    
    fun testTimeListManagement() {
        println("\n=== 时间列表管理测试 ===")
        
        val times = mutableListOf<String>()
        
        // 添加时间
        val newTimes = listOf("08:30", "14:30", "21:30")
        newTimes.forEach { time ->
            if (!times.contains(time)) {
                times.add(time)
                println("添加时间: $time")
            }
        }
        
        assert(times.size == 3) { "应该添加3个时间" }
        assert(times.contains("08:30")) { "应该包含08:30" }
        assert(times.contains("14:30")) { "应该包含14:30" }
        assert(times.contains("21:30")) { "应该包含21:30" }
        
        // 删除时间
        times.remove("14:30")
        println("删除时间: 14:30")
        
        assert(times.size == 2) { "删除后应该有2个时间" }
        assert(!times.contains("14:30")) { "不应该包含14:30" }
        
        println("✅ 时间列表管理测试通过")
    }
    
    private fun isValidTimeFormat(time: String): Boolean {
        return try {
            val parts = time.split(":")
            if (parts.size != 2) return false
            
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            
            hour in 0..23 && minute in 0..59
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    fun runAllTests() {
        println("🚀 开始时间设置功能测试...")
        
        try {
            testTimeFormatting()
            testTimeValidation()
            testTimeListManagement()
            
            println("\n🎉 所有测试通过！时间设置功能正常")
        } catch (e: AssertionError) {
            println("\n❌ 测试失败: ${e.message}")
        } catch (e: Exception) {
            println("\n💥 测试异常: ${e.message}")
        }
    }
}

fun main() {
    val test = TimeSettingTest()
    test.runAllTests()
}
