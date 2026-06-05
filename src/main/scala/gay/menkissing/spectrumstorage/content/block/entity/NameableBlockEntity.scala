package gay.menkissing.spectrumstorage.content.block.entity

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.{CompoundTag, Tag}
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Component.Serializer
import net.minecraft.world.Nameable
import net.minecraft.world.level.block.entity.BlockEntity

trait NameableBlockEntity extends BlockEntity, Nameable:
  var name: Option[Component] = None

  def defaultName: Component

  override def getName: Component =
    name.getOrElse(defaultName)

  def setCustomName(name: Component): Unit =
    this.name = Option(name)

  override def getCustomName: Component =
    name.orNull

  override def loadAdditional(tag: CompoundTag, provider: HolderLookup.Provider): Unit =
    super.loadAdditional(tag, provider)
    if tag.contains("CustomName", Tag.TAG_STRING) then
      this.name = Option(Serializer.fromJson(tag.getString("CustomName"), provider))

  override protected def saveAdditional(tag: CompoundTag, provider: HolderLookup.Provider): Unit =
    super.saveAdditional(tag, provider)
    name.foreach: name =>
      tag.putString("CustomName", Serializer.toJson(name, provider))
