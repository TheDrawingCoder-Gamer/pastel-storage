package gay.menkissing.spectrumstorage.util

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.registries.{BuiltInRegistries, Registries}
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.{ByteBufCodecs, StreamCodec}
import net.minecraft.world.level.material.{FlowingFluid, Fluid, Fluids}
import net.neoforged.neoforge.fluids.{FluidStack, FluidType}

import java.util.Objects

final class FluidResource private (val fluid: Fluid, val components: DataComponentPatch) {
  override val hashCode: Int = Objects.hash(fluid, components)

  def isBlank: Boolean =
    fluid == Fluids.EMPTY

  def componentsMatch(other: DataComponentPatch): Boolean =
    this.components == other

  def makeStack(size: Int): FluidStack =
    if size == 0 then
      FluidStack.EMPTY
    else
      FluidStack(fluid.builtInRegistryHolder(), size, components)

  def sameAsStack(stack: FluidStack): Boolean =
    this.fluid == stack.getFluid && this.componentsMatch(stack.getComponentsPatch)
  
  override def equals(obj: Any): Boolean =
    if (this eq obj.asInstanceOf[Object]) true
    else if (obj == null || getClass != obj.getClass) false
    else
      val that = obj.asInstanceOf[FluidResource]
      hashCode == that.hashCode && fluid == that.fluid && componentsMatch(that.components)
}

object FluidResource:
  
  val CODEC: Codec[FluidResource] =
    RecordCodecBuilder.create: builder =>
      builder.group(
        BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter((it: FluidResource) => it.fluid),
        DataComponentPatch.CODEC.fieldOf("components").forGetter((it: FluidResource) => it.components)
      ).apply(builder, FluidResource.apply)
  val STREAM_CODEC: StreamCodec[RegistryFriendlyByteBuf, FluidResource] =
    StreamCodec.composite(
      ByteBufCodecs.registry(Registries.FLUID), (it: FluidResource) => it.fluid,
      DataComponentPatch.STREAM_CODEC, (it: FluidResource) => it.components,
      FluidResource.of
    )



  val EMPTY = FluidResource(Fluids.EMPTY, DataComponentPatch.EMPTY)

  def ofStack(stack: FluidStack): FluidResource =
    Objects.requireNonNull(stack, "Fluid stack may not be null")

    FluidResource(stack.getFluid, stack.getComponentsPatch)

  def of(fluid: Fluid): FluidResource =
    of(fluid, DataComponentPatch.EMPTY)
  
  def of(fluid: Fluid, components: DataComponentPatch): FluidResource =
    Objects.requireNonNull(fluid, "Fluid may not be null")
    Objects.requireNonNull(components, "Components may not be null")

    val goodFluid =
      if (!fluid.isSource(fluid.defaultFluidState()) && fluid != Fluids.EMPTY) {
        fluid match
          case it: FlowingFluid => it.getSource()
          case _ =>
            val id = BuiltInRegistries.FLUID.getKey(fluid)
            throw new IllegalArgumentException(s"Can't convert flowing fluid $id ($fluid) into a still fluid")
      } else fluid

    FluidResource(goodFluid, components)

