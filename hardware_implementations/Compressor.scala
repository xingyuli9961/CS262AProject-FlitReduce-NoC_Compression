// Tushar: This is a 8-cycle-latency 16bit Zero-Encoding Compressor designed for the CS262A class project
import chisel3._
import chisel3.util._


class Compressor(tlbundle_params : TLBundleParameters, cParam : BaseChannelParams, egressIDBits : Int)(implicit val p: Parameters) extends Module {
    val io = IO(new Bundle {
        val in = Flipped(Decoupled(new TLBundleD(tlbundle_params)))
        val egress_id = Input(UInt(egressIDBits.W))
        val out = Decoupled(new IOFlit(cParam))
    })

    val input_buffer = RegInit(VecInit(Seq.fill(8)(0.U.asTypeOf(new TLBundleD(tlbundle_params)))))
    val egressID_buffer = Reg(UInt(egressIDBits.W))
    
    val index = RegInit(0.U(3.W))
    
    val pop :: comp :: ignore :: Nil = Enum(3)
    val state = RegInit(0.U(2.W)) // 0-populating / idle 1-compressing 2-ignore compression
    
    val header_bitmap = Wire(Vec(32, UInt(1.W)))
    val maximum_index = Wire(UInt(5.W))
    maximum_index := 0.U
    for (i <- 0 to 31) {
        //Bitmap : 1 is zeros, 0 is data (1 is compressed, 0 is not compressed)
        header_bitmap(31 - i) := input_buffer(i/4).data(16*(i%4) + 15, 16*(i%4)) === 0.U
        when(header_bitmap(31 -i) === 0.U) {
            maximum_index := i.U        
        }
    }
    val header_payload = Wire(Vec(2, UInt(16.W)))
    val general_payload = Wire(Vec(4, UInt(16.W)))
    val payload_indices = Wire(Vec(4, UInt(5.W)))
    val last_index = Reg(UInt(5.W))
    when(state === 0.U) {
        io.in.ready := true.B
        io.out.valid := false.B
        io.out.bits.head := false.B
        io.out.bits.tail := false.B
        io.out.bits.payload := 0.U
        io.out.bits.egress_id := 0.U
        when(io.in.fire()) {
            input_buffer(index) := io.in.bits
            egressID_buffer := io.egress_id
            when(index === 7.U) {
                index := 0.U
                state := 1.U
            }.otherwise{
                index := index + 1.U
                state := 0.U
            }  
        }
        header_payload(0) := 0.U
        header_payload(1) := 0.U
        general_payload(0) := 0.U
        general_payload(1) := 0.U
        general_payload(2) := 0.U
        general_payload(3) := 0.U
        payload_indices(0) := 0.U
        payload_indices(1) := 0.U
        payload_indices(2) := 0.U
        payload_indices(3) := 0.U
    }.elsewhen(state === 1.U) {
        val tlbundle_out = Wire(new TLBundleD(tlbundle_params))
        val compressed_out = Wire(UInt(1.W))
        io.out.bits.payload := Cat(compressed_out, tlbundle_out.asUInt)
        io.out.valid := true.B
        io.in.ready := false.B
        header_payload(0) := 0.U
        header_payload(1) := 0.U
        general_payload(0) := 0.U
        general_payload(1) := 0.U
        general_payload(2) := 0.U
        general_payload(3) := 0.U
        payload_indices(0) := 0.U
        payload_indices(1) := 0.U
        payload_indices(2) := 0.U
        payload_indices(3) := 0.U
        when(Cat(header_bitmap) === 0.U) {
            tlbundle_out := input_buffer(index)
            compressed_out := false.B
            io.out.bits.egress_id := egressID_buffer 
            when(io.out.fire()) {
                index := index + 1.U
            } 
            when(index === 7.U) {
                when(io.out.fire()) {
                    state := 0.U
                    index := 0.U
                }
                io.out.bits.head := false.B
                io.out.bits.tail := true.B
            }.elsewhen(index === 0.U) {
                io.out.bits.head := true.B
                io.out.bits.tail := false.B
            }.otherwise {
                io.out.bits.head := false.B
                io.out.bits.tail := false.B
            }
        }.otherwise{
            tlbundle_out := input_buffer(0)
            compressed_out:= true.B
            when (io.out.fire()) {
                index := index + 1.U
            }
            when(index === 0.U) {
                io.out.bits.head := true.B
                io.out.bits.egress_id := egressID_buffer
                tlbundle_out.data := Cat(Cat(header_payload), Cat(header_bitmap))

                for(i <- 0 to 31) {
                    when(!header_bitmap(i).asBool) {
                        header_payload(1) := input_buffer((31 - i)/4).data(16*((31 - i)%4) + 15, 16*((31 - i)%4))
                        payload_indices(1) := (31 - i).U
                        when(io.out.fire()) {    
                            last_index := payload_indices(1)
                        }
                    }
                }
                for(i <- 0 to 31) {
                    when(!header_bitmap(i).asBool && (31 - i).U > payload_indices(1)) {
                        header_payload(0) := input_buffer((31 - i)/4).data(16*((31 - i)%4) + 15, 16*((31 - i)%4))
                        payload_indices(0) := (31 - i).U
                        when(io.out.fire()) {  
                            last_index := payload_indices(0)
                        }
                    }
                }
            }.otherwise {
                io.out.bits.head := false.B
                io.out.bits.egress_id := egressID_buffer
                tlbundle_out.data := Cat(general_payload)
                for(i <- 0 to 31) {
                    when(!header_bitmap(i.U).asBool && (31 - i).U > last_index) {
                        general_payload(3) := input_buffer((31 - i)/4).data(16*((31 - i)%4) + 15, 16*((31 - i)%4))
                        payload_indices(3) := (31 - i).U
                        when(io.out.fire()) {    
                            last_index := payload_indices(3)
                        }
                    }
                }
                for(i <- 0 to 31) {
                    when(!header_bitmap(i.U).asBool && (31 - i).U > payload_indices(3)) {
                        general_payload(2) := input_buffer((31 - i)/4).data(16*((31 - i)%4) + 15, 16*((31 - i)%4))
                        payload_indices(2) := (31 - i).U
                        when(io.out.fire()) {    
                            last_index := payload_indices(2)
                        }
                    }
                }
                for(i <- 0 to 31) {
                    when(!header_bitmap(i.U).asBool && (31 - i).U > payload_indices(2) && (31 - i).U > payload_indices(3)) {
                        general_payload(1) := input_buffer((31 - i)/4).data(16*((31 - i)%4) + 15, 16*((31 - i)%4))
                        payload_indices(1) := (31 - i).U
                        when(io.out.fire()) {    
                            last_index := payload_indices(1)
                        }
                    }
                }
                for(i <- 0 to 31) {
                    when(!header_bitmap(i.U).asBool && (31 - i).U > payload_indices(1) && (31 - i).U > payload_indices(2) && (31 - i).U > payload_indices(3)) {
                        general_payload(0) := input_buffer((31 - i)/4).data(16*((31 - i)%4) + 15, 16*((31 - i)%4))
                        payload_indices(0) := (31 - i).U
                        when(io.out.fire()) {    
                            last_index := payload_indices(0)
                        }
                    }
                }
            }
            io.out.bits.tail := false.B
            for (i <- 0 to 3) {
                when(payload_indices(i) === maximum_index && io.out.fire()) {
                    io.out.bits.tail := true.B
                    state := 0.U
                    index := 0.U
                    last_index := 0.U
                }
            }
        }
    }.otherwise{ //should be for skipped packets for some io stuff
        io.out.valid := true.B
        io.in.ready := false.B
        io.out.bits.head := false.B
        io.out.bits.tail := false.B
        io.out.bits.payload := 0.U
        io.out.bits.egress_id := 0.U
        
        header_payload(0) := 0.U
        header_payload(1) := 0.U
        general_payload(0) := 0.U
        general_payload(1) := 0.U
        general_payload(2) := 0.U
        general_payload(3) := 0.U
        payload_indices(0) := 0.U
        payload_indices(1) := 0.U
        payload_indices(2) := 0.U
        payload_indices(3) := 0.U
    }
}
