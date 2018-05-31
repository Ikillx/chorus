package eu.iamgio.chorus.menus.drop

/**
 * @author Gio
 */
class ColoredTextDropMenu : DropMenu() {

    override fun getButtons(): MutableList<DropMenuButton> = arrayListOf(
            DropMenuButton("Chat preview", "colored"),
            DropMenuButton("Title preview", "colored"),
            DropMenuButton("Scoreboard preview", "colored"),
            DropMenuButton("Item preview", "colored"),
            DropMenuButton("Sign preview", "colored"),
            DropMenuButton("Action bar preview", "colored"),
            DropMenuButton("Boss bar preview", "colored"),
            DropMenuButton("Mob bar preview", "colored"),
            DropMenuButton("MOTD preview", "colored"),
            DropMenuButton("Animated text preview", "colored")
    )
}