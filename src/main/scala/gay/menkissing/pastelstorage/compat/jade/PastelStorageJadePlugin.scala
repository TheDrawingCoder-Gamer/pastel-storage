package gay.menkissing.pastelstorage.compat.jade

import gay.menkissing.pastelstorage.content.block.entity.BottomlessStorageBlockEntity
import snownee.jade.api.{IWailaClientRegistration, IWailaCommonRegistration, IWailaPlugin, WailaPlugin}

@WailaPlugin
class PastelStorageJadePlugin extends IWailaPlugin:
  override def register(registration: IWailaCommonRegistration): Unit =
    registration.registerEnergyStorage(BigEnergyStorageProvider, classOf[BottomlessStorageBlockEntity])

  override def registerClient(registration: IWailaClientRegistration): Unit =
    registration.registerEnergyStorageClient(BigEnergyStorageProvider)
