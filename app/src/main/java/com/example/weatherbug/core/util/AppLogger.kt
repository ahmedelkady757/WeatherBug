package com.example.weatherbug.core.util


import android.util.Log
import org.json.JSONArray
import org.json.JSONObject


object AppLogger {

    private const val TAG_HTTP      = "WB_HTTP"
    private const val TAG_DB        = "WB_DB"
    private const val TAG_DATASTORE = "WB_DATASTORE"
    private const val TAG_REPO      = "WB_REPO"
    private const val TAG_VM        = "WB_VM"
    private const val TAG_WORKER    = "WB_WORKER"
    private const val TAG_NAV       = "WB_NAV"
    private const val TAG_GENERAL   = "WB_GENERAL"

    // ── HTTP ─────────────────────────────────────────────────────────────────


    fun logRequest(url: String, method: String) {
        Log.d(TAG_HTTP, buildString {
            appendLine("━━━━━━━━━━━━━━━━━━ ➤ REQUEST ━━━━━━━━━━━━━━━━━━")
            appendLine("  METHOD : $method")
            appendLine("  URL    : $url")
            append    ("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        })
    }


    fun logResponse(url: String, code: Int, durationMs: Long, body: String?) {
        val level = if (code in 200..299) Log.DEBUG else Log.WARN
        Log.println(level, TAG_HTTP, buildString {
            appendLine("━━━━━━━━━━━━━━━━━━ ◀ RESPONSE ━━━━━━━━━━━━━━━━━━")
            appendLine("  URL      : $url")
            appendLine("  CODE     : $code")
            appendLine("  DURATION : ${durationMs}ms")
            body?.let {
                appendLine("  BODY     :")
                appendLine(prettyJson(it).prependIndent("    "))
            }
            append    ("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        })
    }


    fun logNetworkError(url: String, error: Throwable) {
        Log.e(TAG_HTTP, buildString {
            appendLine("━━━━━━━━━━━━━━━━━━ ✖ NETWORK ERROR ━━━━━━━━━━━━━")
            appendLine("  URL   : $url")
            appendLine("  ERROR : ${error.javaClass.simpleName}: ${error.message}")
            append    ("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        })
    }


    fun logDbInsert(table: String, data: Any) {
        Log.d(TAG_DB, "INSERT → [$table] | $data")
    }

    fun logDbDelete(table: String, identifier: Any) {
        Log.d(TAG_DB, "DELETE → [$table] | id = $identifier")
    }

    fun logDbDeleteAll(table: String) {
        Log.d(TAG_DB, "DELETE ALL → [$table]")
    }

    fun logDbQuery(table: String, resultSummary: String) {
        Log.d(TAG_DB, "QUERY  ← [$table] | $resultSummary")
    }

    fun logDbError(table: String, error: Throwable) {
        Log.e(TAG_DB, "ERROR  → [$table] | ${error.javaClass.simpleName}: ${error.message}")
    }


    fun logDataStoreWrite(key: String, value: Any?) {
        Log.d(TAG_DATASTORE, "WRITE → [$key] = $value")
    }

    fun logDataStoreRead(key: String, value: Any?) {
        Log.d(TAG_DATASTORE, "READ  ← [$key] = $value")
    }

    fun logDataStoreError(key: String, error: Throwable) {
        Log.e(TAG_DATASTORE, "ERROR → [$key] | ${error.javaClass.simpleName}: ${error.message}")
    }


    fun logRepoCall(method: String, params: String) {
        Log.d(TAG_REPO, "CALL  → $method($params)")
    }

    fun logRepoCacheHit(key: String) {
        Log.d(TAG_REPO, "CACHE HIT  ← $key")
    }

    fun logRepoCacheMiss(key: String) {
        Log.d(TAG_REPO, "CACHE MISS ↓ fetching remote for $key")
    }

    fun logRepoError(method: String, error: Throwable) {
        Log.e(TAG_REPO, "ERROR → $method | ${error.javaClass.simpleName}: ${error.message}")
    }


    fun logVmEvent(vmName: String, event: String) {
        Log.d(TAG_VM, "[$vmName] $event")
    }

    fun logVmError(vmName: String, message: String) {
        Log.e(TAG_VM, "[$vmName] ERROR: $message")
    }


    fun logWorkerStart(workerName: String, inputData: String) {
        Log.d(TAG_WORKER, "▶ START   [$workerName] input = $inputData")
    }

    fun logWorkerSuccess(workerName: String) {
        Log.d(TAG_WORKER, "✔ SUCCESS [$workerName]")
    }

    fun logWorkerRetry(workerName: String, reason: String) {
        Log.w(TAG_WORKER, "↺ RETRY   [$workerName] reason = $reason")
    }

    fun logWorkerFailed(workerName: String, reason: String) {
        Log.e(TAG_WORKER, "✖ FAILED  [$workerName] reason = $reason")
    }


    fun logNavigation(from: String, to: String, args: String? = null) {
        val argsStr = args?.let { " | args = $it" } ?: ""
        Log.d(TAG_NAV, "NAVIGATE: $from → $to$argsStr")
    }


    fun d(message: String, tag: String = TAG_GENERAL) {
        Log.d(tag, message)
    }

    fun w(message: String, tag: String = TAG_GENERAL) {
        Log.w(tag, message)
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = TAG_GENERAL) {
        Log.e(tag, message, throwable)
    }



    private fun prettyJson(raw: String): String {
        return try {
            val trimmed = raw.trim()
            when {
                trimmed.startsWith("{") -> {
                    JSONObject(trimmed).toString(2)
                }
                trimmed.startsWith("[") -> {
                    JSONArray(trimmed).toString(2)
                }
                else -> raw
            }
        } catch (e: Exception) {
            raw
        }
    }
}