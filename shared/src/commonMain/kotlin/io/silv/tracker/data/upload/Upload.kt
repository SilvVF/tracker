import io.ktor.client.content.ProgressListener
import io.silv.tracker.data.logs.Log
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlin.jvm.Transient

class UploadImage(
    val index: Int,
    val path: String?
): ProgressListener  {

    @kotlinx.serialization.Transient
    private val _progressFlow = MutableStateFlow(0)

    @kotlinx.serialization.Transient
    val progressFlow = _progressFlow.asStateFlow()

    var progress: Int
        get() = _progressFlow.value
        set(value) {
            _progressFlow.value = value
        }

    @kotlinx.serialization.Transient
    private val _statusFlow = MutableStateFlow(UploadImage.State.QUEUE)

    @kotlinx.serialization.Transient
    val statusFlow = _statusFlow.asStateFlow()

    var status: UploadImage.State
        get() = _statusFlow.value
        set(value) {
            _statusFlow.value = value
        }

    override suspend fun invoke(bytesSentTotal: Long, contentLength: Long) {
        progress =
            if (contentLength > 0) {
                (100 * bytesSentTotal / contentLength).toInt()
            } else {
                -1
            }
    }

    enum class State {
        QUEUE,
        LOAD_PAGE,
        UPLOAD_IMAGE,
        UPLOADED,
        ERROR,
    }
}

class Upload(
    val log: Log,
) {

    var images: List<UploadImage>? = null

    val totalProgress: Int
        get() = images?.sumOf(UploadImage::progress) ?: 0

    val uploadedImages: Int
        get() = images?.count { it.status == UploadImage.State.UPLOADED } ?: 0

    @Transient
    private val _statusFlow = MutableStateFlow(State.NOT_UPLOADED)

    @Transient
    val statusFlow = _statusFlow.asStateFlow()
    var status: State
        get() = _statusFlow.value
        set(status) {
            _statusFlow.value = status
        }

    @OptIn(FlowPreview::class)
    @Transient
    val progressFlow =
        flow {
            if (images == null) {
                emit(0)
                while (images == null) {
                    delay(50)
                }
            }

            val progressFlows = images!!.map(UploadImage::progressFlow)
            emitAll(combine(progressFlows) { it.average().toInt() })
        }
            .distinctUntilChanged()
            .debounce(50)

    val progress: Int
        get() {
            val images = images ?: return 0
            return images.map(UploadImage::progress).average().toInt()
        }

    enum class State(val value: Int) {
        NOT_UPLOADED(0),
        QUEUE(1),
        UPLOADING(2),
        UPLOADED(3),
        ERROR(4),
    }
}