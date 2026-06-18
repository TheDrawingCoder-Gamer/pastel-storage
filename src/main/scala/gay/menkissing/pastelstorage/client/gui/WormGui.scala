package gay.menkissing.pastelstorage.client.gui

import gay.menkissing.pastelstorage.screen.BottomlessWormMenu
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.neoforged.api.distmarker.{Dist, OnlyIn}

@OnlyIn(/* ohio */ Dist.CLIENT)
class WormGui(menu: BottomlessWormMenu, inventory: Inventory, component: Component) extends AbstractContainerScreen[BottomlessWormMenu](menu, inventory, component):
  override def renderBg(guiGraphics: GuiGraphics, f: Float, i: Int, j: Int): Unit =
    val i = (this.width - this.imageWidth) / 2
    val j = (this.height - this.imageHeight) / 2
    guiGraphics.blit(WormGui.TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight)

  override def render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float): Unit =
    this.renderBackground(guiGraphics, mouseX, mouseY, delta)
    super.render(guiGraphics, mouseX, mouseY, delta)
    this.renderTooltip(guiGraphics, mouseX, mouseY)
  
object WormGui:
  val TEXTURE = ResourceLocation.fromNamespaceAndPath("pastel", "textures/gui/container/generic_3x3_tier_1.png")
