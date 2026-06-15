package gay.menkissing.pastelstorage.util

import net.neoforged.neoforge.fluids.FluidType


object PastelStorageNumberFormatting:
  def formatFE(fe: Int): String =
    if fe < 1000 then
      fe.toString + " FE"
    else if fe < 1000000 then
      String.format("%1$.2f KFE", fe / 1000f)
    else
      String.format("%1$.2f MFE", fe / 1000000f)
  
  def formatMB(mb: Int): String =
    if mb < 1000 then
      mb.toLong.toString
    else if mb < 1000000 then
      String.format("%1$.2fK", mb / 1000f)
    else
      String.format("%1$.2fM", mb / 1000000f)

  def formatFluidMax(amountMb: Int): String =
    val buckets = math.round(amountMb.toFloat / FluidType.BUCKET_VOLUME.toFloat)
    buckets.toString
