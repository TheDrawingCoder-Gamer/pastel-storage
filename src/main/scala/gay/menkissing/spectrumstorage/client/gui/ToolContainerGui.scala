package gay.menkissing.spectrumstorage.client.gui

import de.dafuqs.spectrum.inventories.ScreenBackgroundVariant
import gay.menkissing.spectrumstorage.screen.ToolContainerMenu
import net.fabricmc.api.{EnvType, Environment}
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

@Environment(EnvType.CLIENT)
object ToolContainerGui:
  def apply(menu: ToolContainerMenu, inventory: Inventory, component: Component): SharedContainerGui[ToolContainerMenu] =
    new SharedContainerGui[ToolContainerMenu](ToolContainerMenu.rows, ScreenBackgroundVariant.EARLYGAME, menu, inventory, component)
