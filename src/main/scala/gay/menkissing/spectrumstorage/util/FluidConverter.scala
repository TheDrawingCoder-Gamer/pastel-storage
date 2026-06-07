package gay.menkissing.spectrumstorage.util

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.neoforged.neoforge.fluids.FluidType

object FluidConverter:
  final val DropletMbConversion = FluidConstants.BUCKET / FluidType.BUCKET_VOLUME

  def dropletToMb(droplet: Long): Int =
    (droplet / DropletMbConversion).toInt

  def mbToDroplet(mb: Int): Long =
    mb.toLong * DropletMbConversion
