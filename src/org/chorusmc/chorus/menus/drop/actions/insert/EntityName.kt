package org.chorusmc.chorus.menus.drop.actions.insert

import org.chorusmc.chorus.minecraft.McClass
import org.chorusmc.chorus.minecraft.entity.Entity
import org.chorusmc.chorus.settings.SettingsBuilder

/**
 * @author Gio
 */
class EntityName : EnumNameAction(McClass(Entity::class.java).cls) {

    init {
        SettingsBuilder.addAction("4.Minecraft.0.Server_version", Runnable {
            enumClass = McClass(Entity::class.java).cls
        })
    }
}