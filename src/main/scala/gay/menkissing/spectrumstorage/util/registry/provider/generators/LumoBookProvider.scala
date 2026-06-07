package gay.menkissing.spectrumstorage.util.registry.provider.generators

import gay.menkissing.spectrumstorage.util.registry.book.{BookEntry, EntryLocation, FalseCategory}
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput.Target
import net.minecraft.data.{CachedOutput, DataProvider, PackOutput}
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.common.data.LanguageProvider

import java.util.concurrent.CompletableFuture
import scala.collection.mutable

// guidebook pages
abstract class LumoBookProvider(modid: String, val output: PackOutput, val lookup: CompletableFuture[HolderLookup.Provider]) extends LanguageProvider(output, modid, "en_us"):
  val bookEntries = mutable.HashMap.empty[EntryLocation, BookEntry]

  def addEntry(location: EntryLocation, category: ResourceLocation, transId: ResourceLocation)(creator: BookEntry => BookEntry): Unit =
    val baseEntry = BookEntry(location, "", transId).withCategory(FalseCategory(category))
    val resEntry = creator(baseEntry)
    resEntry.submit(this)

    bookEntries(location) = resEntry


  protected def addEntries(lookup: HolderLookup.Provider): Unit

  override def run(cachedOutput: CachedOutput): CompletableFuture[?] =
    lookup.thenCompose: lookup =>
      addEntries(lookup)
      CompletableFuture.allOf(
        super.run(cachedOutput),
        CompletableFuture.allOf(
          bookEntries.map: (k, v) =>
            val path =
              output.getOutputFolder(Target.DATA_PACK)
                    .resolve(k.book.getNamespace)
                    .resolve("modonomicon/books")
                    .resolve(k.book.getPath)
                    .resolve("entries")
            DataProvider.saveStable(cachedOutput, v.toJson(lookup), path.resolve(v.location.id.getPath + ".json"))
          .toSeq*
        )
      )


  override def getName: String = "Spectrum storage book provider"
