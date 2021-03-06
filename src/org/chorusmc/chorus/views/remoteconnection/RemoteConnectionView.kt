package org.chorusmc.chorus.views.remoteconnection

import javafx.beans.property.SimpleBooleanProperty
import javafx.css.PseudoClass
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.chorusmc.chorus.Chorus
import org.chorusmc.chorus.connection.Password
import org.chorusmc.chorus.connection.RemoteConnection
import org.chorusmc.chorus.nodes.control.NumericTextField
import org.chorusmc.chorus.util.config
import org.chorusmc.chorus.util.toObservableList
import org.chorusmc.chorus.util.translate
import org.chorusmc.chorus.views.View

/**
 * @author Gio
 */
open class RemoteConnectionView(private val name: String, defaultPort: Int, setting: String, private val psw: Password) : View(
        "",
        Image(Chorus::class.java.getResourceAsStream("/assets/images/icon.png")),
        800.0,
        400.0
) {

    private var ips = emptyMap<String, Triple<String, String, String?>>()

    private val ip = ComboBox<String>()
    private val username = TextField()
    private val password = PasswordField()
    private val port = NumericTextField()
    val connectButton = Button()
    private val filesBox = VBox()

    lateinit var onSelect: Runnable
    lateinit var selectedPath: String

    var title = "Chorus - $name"
        set(value) {
            stage.title = value
        }

    fun onConfirm(unit: (String, String, Int, String, Boolean) -> Unit) = connectButton.setOnAction {
        unit(ip.selectionModel.selectedItem, username.text, port.text.toInt(), password.text, ips[ip.selectionModel.selectedItem]!!.third != null)
    }

    var onBrowse: (Pair<String, String>) -> Unit = {}

    init {
        config[setting].split("\\n").forEach {
            with(it.split("|")) {
                ips += this[0] to when(size) {
                    2 -> Triple(this[1], defaultPort.toString(), null)
                    3 -> Triple(this[1], this[2], null)
                    4 -> if(name == "SFTP") Triple(this[1], this[2], this[3]) else return@with
                    else -> return@with
                }
            }
        }
    }

    override fun show() {
        val root = VBox()
        root.styleClass.addAll("pane", "sftp-pane")
        val addressHbox = HBox(ip)
        ip.selectionModel.selectedItemProperty().addListener {_ ->
            val triple = ips[ip.selectionModel.selectedItem]!!
            username.text = triple.first
            port.text = triple.second
            if(triple.third == null && addressHbox.children.size > 2 && !addressHbox.children.contains(password)) {
                addressHbox.children.add(2, password)
            } else if(triple.third != null && addressHbox.children.contains(password)) {
                addressHbox.children -= password
            }
            password.requestFocus()
        }
        ip.promptText = translate("${name.toLowerCase()}.no_server")
        ip.items = ips.keys.toList().toObservableList()
        if(ip.items.size > 0) ip.selectionModel.selectFirst() else ip.isDisable = true
        ip.styleClass += "ip-box"
        username.promptText = translate("remote.username")
        username.styleClass += "username-field"
        password.text = String(psw.psw)
        password.promptText = translate("remote.password")
        password.styleClass += "password-field"
        port.promptText = translate("remote.port")
        port.styleClass += "username-field"
        port.prefWidth = 40.0
        port.alignment = Pos.CENTER
        username.setOnAction {
            connectButton.fire()
        }
        port.disableProperty().bind(ip.selectionModel.selectedItemProperty().isNull)
        username.disableProperty().bind(ip.selectionModel.selectedItemProperty().isNull)
        password.setOnAction {
            connectButton.fire()
        }
        password.disableProperty().bind(ip.selectionModel.selectedItemProperty().isNull)
        connectButton.text = translate("remote.connect")
        connectButton.styleClass += "connect-button"
        connectButton.disableProperty().bind(
                ip.selectionModel.selectedItemProperty().isNull
                        .or(username.textProperty().isEmpty)
                        .or((password.textProperty().isEmpty).and(SimpleBooleanProperty(ips[ip.selectionModel.selectedItem]?.third == null)))
                        .or(port.textProperty().isEmpty)
        )
        port.setOnAction {
            connectButton.fire()
        }
        if(ip.selectionModel.selectedItem != null) {
            addressHbox.children.addAll(username, port, connectButton)
            if(ips[ip.selectionModel.selectedItem]!!.third == null) addressHbox.children.add(2, password)
        }
        addressHbox.styleClass += "sftp-address-box"
        addressHbox.alignment = Pos.CENTER
        addressHbox.spacing = 15.0
        addressHbox.prefHeight = 75.0
        addressHbox.minHeight = addressHbox.prefHeight
        addressHbox.prefWidthProperty().bind(root.prefWidthProperty())
        val scrollpane = ScrollPane(filesBox)
        stage.title = title
        val scene = setRoot(root)
        scrollpane.prefWidthProperty().bind(scene.widthProperty())
        scrollpane.prefHeightProperty().bind(scene.heightProperty())
        scrollpane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        filesBox.styleClass += "files-box"
        filesBox.prefWidthProperty().bind(scrollpane.prefWidthProperty())
        val mainVbox = VBox(addressHbox, scrollpane)
        root.children += mainVbox
        password.requestFocus()
    }

    fun close() = stage.close()

    fun clear() = filesBox.children.clear()

    fun generateFiles(connection: RemoteConnection, loc: String = connection.home) {
        filesBox.children.clear()
        var location = ""
        if(loc.endsWith("/..")) {
            val parts = loc.split("/")
            (0 until parts.size - 2).forEach {
                location += parts[it] + "/"
            }
            if(parts.size - 3 > 0) {
                location = location.substring(0, location.length - 1)
            }
        } else location = loc
        val files = try {
            connection.getFiles(location)
        } catch(e: Exception) {
            location = connection.home
            connection.getFiles(location)
        }
        title = "Chorus - $name [$location]"
        files.filter {it.filename != "."}.sortedBy {it.filename}.sortedBy {!it.isDir}.forEach {
            if(!(location == "/" && it.filename == "..")) {
                val button = RemoteConnectionButton(it.filename, "$location${if(location.endsWith("/")) "" else "/"}${it.filename}", this, connection, it.isDir)
                button.prefWidthProperty().bind(filesBox.prefWidthProperty())
                button.addEventFilter(MouseEvent.MOUSE_PRESSED) {
                    filesBox.children.filtered {it != button}.forEach {
                        it.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), false)
                    }
                }
                filesBox.children += button
            }
        }
    }
}