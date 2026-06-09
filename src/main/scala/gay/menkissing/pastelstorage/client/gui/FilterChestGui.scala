package gay.menkissing.pastelstorage.client.gui

import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.screen.FilterChestMenu
import net.neoforged.api.distmarker.{Dist, OnlyIn}
// import net.fabricmc.api.{EnvType, Environment}
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

// @Environment(EnvType.CLIENT)
@OnlyIn(Dist.CLIENT)
class FilterChestGui(menu: FilterChestMenu, playerInv: Inventory, title: Component) extends AbstractContainerScreen[FilterChestMenu](menu, playerInv, title):
  override def renderBg(guiGraphics: GuiGraphics, f: Float, i: Int, j: Int): Unit =
    val i = (this.width - this.imageWidth) / 2
    val j = (this.height - this.imageHeight) / 2
    guiGraphics.blit(FilterChestGui.texture, i, j, 0, 0, this.imageWidth, this.imageHeight)

  override def render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float): Unit =
    this.renderBackground(guiGraphics, mouseX, mouseY, delta)
    super.render(guiGraphics, mouseX, mouseY, delta)
    this.renderTooltip(guiGraphics, mouseX, mouseY)
  
object FilterChestGui:
  val texture = PastelStorage.locate("textures/gui/container/filter_chest.png")
