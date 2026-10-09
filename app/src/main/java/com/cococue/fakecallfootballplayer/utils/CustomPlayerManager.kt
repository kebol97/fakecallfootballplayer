package com.cococue.fakecallfootballplayer.utils

import android.content.Context
import com.cococue.fakecallfootballplayer.model.FootballPlayer
import org.json.JSONArray
import org.json.JSONObject

object CustomPlayerManager {
    private const val PREF_NAME = "custom_players_pref"
    private const val KEY_PLAYERS = "custom_players_json"

    fun getCustomPlayers(context: Context): List<FootballPlayer> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_PLAYERS, null) ?: return emptyList()
        val list = mutableListOf<FootballPlayer>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val avatarUri = if (obj.has("customAvatarUri")) obj.getString("customAvatarUri") else null
                val videoUri = if (obj.has("customVideoUri")) obj.getString("customVideoUri") else null
                list.add(
                    FootballPlayer(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        clubInfo = obj.optString("clubInfo", "Custom Star"),
                        phoneNumber = obj.optString("phoneNumber", "+1 555 0199"),
                        customAvatarUri = avatarUri,
                        customVideoUri = videoUri
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveCustomPlayer(context: Context, player: FootballPlayer) {
        val players = getCustomPlayers(context).toMutableList()
        players.add(player)
        val array = JSONArray()
        for (p in players) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("clubInfo", p.clubInfo)
                put("phoneNumber", p.phoneNumber)
                put("customAvatarUri", p.customAvatarUri)
                put("customVideoUri", p.customVideoUri)
            }
            array.put(obj)
        }
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PLAYERS, array.toString()).apply()
    }
}
