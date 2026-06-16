package gay.menkissing.pastelstorage.compat.jade

import com.mojang.serialization.{Codec, MapCodec}
import com.mojang.serialization.codecs.RecordCodecBuilder
import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.content.block.entity.BottomlessStorageBlockEntity
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.{CompoundTag, NbtOps, Tag}
import net.minecraft.network.codec.{ByteBufCodecs, StreamCodec}
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.capabilities.Capabilities
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.ui.IElementHelper
import snownee.jade.api.view.{ClientViewGroup, EnergyView, IClientExtensionProvider, IServerExtensionProvider, ViewGroup}
import snownee.jade.api.{Accessor, BlockAccessor, IComponentProvider, IServerDataProvider, ITooltip}
import snownee.jade.impl.lookup.WrappedHierarchyLookup

import java.util
import java.util.stream.Collectors

object BigEnergyStorageProvider extends IClientExtensionProvider[CompoundTag, EnergyView], IServerExtensionProvider[CompoundTag]:
  val ENERGY_TAG = "pastel_storage_big_energy"
  val BLOCK_DATA = PastelStorage.locate("data_provider")
  val BIG_ENERGY = PastelStorage.locate("big_energy")

  override def getClientGroups(accessor: Accessor[?], list: util.List[ViewGroup[CompoundTag]]): util.List[ClientViewGroup[EnergyView]] =
    list.stream().map: view =>
      ClientViewGroup:
        view.views.stream().flatMap: tag =>
          util.stream.Stream.ofNullable(EnergyView.read(tag, "FE"))
        .collect(Collectors.toList)
    .collect(Collectors.toList[ClientViewGroup[EnergyView]])


  override def getUid: ResourceLocation = BIG_ENERGY

  override def getGroups(accessor: Accessor[?]): util.List[ViewGroup[CompoundTag]] =
    accessor match
      case blockAccessor: BlockAccessor =>
        blockAccessor.getBlockEntity match
          case bsbe: BottomlessStorageBlockEntity =>
            if bsbe.energyStorage.capacityLong > 0 then
              val tag = EnergyView.of(bsbe.energyStorage.energyStoredLong, bsbe.energyStorage.capacityLong)
              util.List.of(ViewGroup(util.List.of(tag)))
            else
              util.List.of()
          case _ => util.List.of()
      case _ =>
        util.List.of()


