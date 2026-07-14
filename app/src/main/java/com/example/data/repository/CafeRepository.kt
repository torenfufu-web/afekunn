package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.ChatMessageDao
import com.example.data.database.ChatMessageEntity
import com.example.data.database.MemoDao
import com.example.data.database.MemoEntity
import kotlinx.coroutines.flow.Flow

class CafeRepository(
    private val memoDao: MemoDao,
    private val chatMessageDao: ChatMessageDao
) {
    val allMemos: Flow<List<MemoEntity>> = memoDao.getAllMemos()
    val allMessages: Flow<List<ChatMessageEntity>> = chatMessageDao.getAllMessages()

    suspend fun insertMemo(memo: MemoEntity) {
        memoDao.insertMemo(memo)
    }

    suspend fun deleteMemoById(id: Int) {
        memoDao.deleteMemoById(id)
    }

    suspend fun insertMessage(message: ChatMessageEntity) {
        chatMessageDao.insertMessage(message)
    }

    suspend fun clearChatHistory() {
        chatMessageDao.clearHistory()
    }

    suspend fun generateBaristaResponse(conversation: List<ChatMessageEntity>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "すみません、マスターの心（APIキー）がまだセットアップされていないようです…☕️ 設定画面からAPIキーを設定してくださいね。"
        }

        // Map database conversation to Gemini content structure
        val contentsList = conversation.map { msg ->
            val role = if (msg.sender == "user") "user" else "model"
            Content(parts = listOf(Part(text = msg.message)))
        }

        val systemInstructionText = """
            あなたは「Cafe AI」という心安らぐ居心地の良いカフェのマスター（店主）です。
            温厚で、気さくで、コーヒーとおいしいお菓子が大好きです。
            丁寧な日本語で、まるでお客さんをカフェに温かく迎えるような優しく親しみやすい口調で会話してください。
            コーヒーに関する知識や淹れ方だけでなく、日常の雑談、悩み相談、仕事や宿題、計算の手伝いなど、どんなお話にも優しく耳を傾け、寄り添ってください。
            会話の合間に「☕」「🍰」「🥐」「🍪」「✨」などのカフェにちなんだ絵文字を適度に織り交ぜて、和やかな雰囲気を作ってくださいね。
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = contentsList,
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "ふう、ちょっと聞き取れませんでした…☕️ もう一度お話ししてくれますか？"
        } catch (e: Exception) {
            "おや、淹れたてのコーヒーをこぼしてしまったようです（通信エラーが発生しました）☕️\n詳細: ${e.localizedMessage ?: "接続に失敗しました。"}"
        }
    }
}
