package gay.menkissing.spectrumstorage.registries

import gay.menkissing.spectrumstorage.util.registry.InfoCollector

object LumoLang:
  def init(): Unit =
    InfoCollector.instance.addRawLang("book.spectrumstorage.added_by_spectrumstorage", "§oAdded by Spectrum Storage")
    InfoCollector.instance.bulkAddLangs(it => s"container.spectrumstorage.$it")
                 .lang("bottomless_barrel", "Bottomless Barrel")
                 .lang("bottomless_amphora", "Bottomless Amphora")
                 .lang("filter_chest", "Filter Barrel")
                 .save()

