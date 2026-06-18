package gay.menkissing.pastelstorage.client

import earth.terrarium.pastel.inventories.ScreenBackgroundVariant
import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.client.gui.{FilterChestGui, SharedContainerGui, ToolContainerGui, WormGui}
import gay.menkissing.pastelstorage.content.PastelStorageBlocks
import gay.menkissing.pastelstorage.registries.PastelStorageScreens
import gay.menkissing.pastelstorage.screen.BottomlessStorageMenu
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.renderer.RenderType
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent

@Mod(value = PastelStorage.ModId, dist = Array(Dist.CLIENT))
class PastelStorageClient(bus: IEventBus):
  // ModelLoadingPlugin.register(new BottomlessBottleModelLoader())
  bus.addListener: (it: RegisterMenuScreensEvent) =>
    it.register(PastelStorageScreens.toolContainer.get(), (a, b, c) => ToolContainerGui(a, b, c))
    it.register(PastelStorageScreens.bottomlessBarrel.get(), (a, b, c) => SharedContainerGui[BottomlessStorageMenu](BottomlessStorageMenu.barrelRows, ScreenBackgroundVariant.EARLYGAME, a, b, c))
    it.register(PastelStorageScreens.bottomlessAmphora.get(), (a, b, c) => SharedContainerGui[BottomlessStorageMenu](BottomlessStorageMenu.amphoraRows, ScreenBackgroundVariant.EARLYGAME, a, b, c))
    it.register(PastelStorageScreens.bottomlessWorm.get(), (a, b, c) => WormGui(a, b, c))
    it.register(PastelStorageScreens.filterChest.get(), (a, b, c) => FilterChestGui(a, b, c))

