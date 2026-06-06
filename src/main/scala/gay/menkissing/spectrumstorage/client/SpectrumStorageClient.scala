package gay.menkissing.spectrumstorage.client

import de.dafuqs.spectrum.inventories.ScreenBackgroundVariant
import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.client.gui.{FilterChestGui, SharedContainerGui, ToolContainerGui}
import gay.menkissing.spectrumstorage.content.SpectrumStorageBlocks
import gay.menkissing.spectrumstorage.content.item.BottomlessBottleModelLoader
import gay.menkissing.spectrumstorage.registries.LumoScreens
import gay.menkissing.spectrumstorage.screen.BottomlessStorageMenu
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.renderer.RenderType
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent

@Mod(value = SpectrumStorage.ModId, dist = Array(Dist.CLIENT))
class SpectrumStorageClient(bus: IEventBus):
  ModelLoadingPlugin.register(new BottomlessBottleModelLoader())
  bus.addListener: (it: RegisterMenuScreensEvent) =>
    it.register(LumoScreens.toolContainer.get(), (a, b, c) => ToolContainerGui(a, b, c))
    it.register(LumoScreens.bottomlessBarrel.get(), (a, b, c) => SharedContainerGui[BottomlessStorageMenu](BottomlessStorageMenu.barrelRows, ScreenBackgroundVariant.EARLYGAME, a, b, c))
    it.register(LumoScreens.bottomlessAmphora.get(), (a, b, c) => SharedContainerGui[BottomlessStorageMenu](BottomlessStorageMenu.amphoraRows, ScreenBackgroundVariant.EARLYGAME, a, b, c))
    it.register(LumoScreens.filterChest.get(), (a, b, c) => FilterChestGui(a, b, c))

