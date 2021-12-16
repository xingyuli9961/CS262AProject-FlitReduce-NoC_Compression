// Xingyu & Tushar:
// Since Constellation NoC is still a project under development and not open to the public, we will not reveal the TLNetwork.scala
// implementation. However, in order to give a taste of how we integrate our customized compressors and decompressors in the system,
// we provde a small code snippet for reference.


// Xingyu: Connect decompressor here
val decompressor = Module(new Decompressor(wide_bundle))
decompressor.io.in.valid := outD.flit.valid
decompressor.io.in.bits := outD.flit.bits.payload.asTypeOf(new TLBundleD(wide_bundle))
decompressor.io.compressed := outD.flit.bits.payload(noc.flitPayloadBits - 1)
decompressor.io.head := outD.flit.bits.head
decompressor.io.tail := outD.flit.bits.tail
decompressor.io.out.ready := in(i).d.ready
in(i).d.valid := decompressor.io.out.valid
in(i).d.bits := decompressor.io.out.bits
outD.flit.ready := decompressor.io.in.ready

// Original code
// in(i).d.valid := outD.flit.valid
// outD.flit.ready := in(i).d.ready
// in(i).d.bits := outD.flit.bits.asTypeOf(new TLBundleD(wide_bundle))


// Tushar: Connect compressor here
val compressor = Module(new Compressor(wide_bundle, inD.flit.bits.cParam, inD.flit.bits.egressIdBits))
compressor.io.egress_id := 1.U +& (requestDOIds(i) * 2.U)
compressor.io.in.valid := out(i).d.valid
compressor.io.in.bits := out(i).d.bits
out(i).d.ready := compressor.io.in.ready
compressor.io.out.ready := inD.flit.ready
inD.flit.valid := compressor.io.out.valid
inD.flit.bits := compressor.io.out.bits
      
// Original code
// inD.flit.valid := out(i).d.valid
// out(i).d.ready := inD.flit.ready
// inD.flit.bits.head := firstDO(i)
// inD.flit.bits.tail := lastDO(i)
// inD.flit.bits.egress_id := 1.U +& (requestDOIds(i) * 2.U)
// inD.flit.bits.payload := out(i).d.bits.asUInt | (Cat(requestDOIds(i), i.U(16.W), tsc << payloadWidth))