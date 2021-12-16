// Xingyu: This is a 2-cycle-latency 16bit Zero-Encoding decompressor designed for the CS262A class project
import chisel3._
import chisel3.util._


// Assume it is used in a controlled environemnt that we will never fetch if there's not enough data and we will never overload the buffer
// Same cycle write and read is enabled
// Be careful about the number to deque when using
class Decompressor_Fifo extends Module {
  val io = IO(new Bundle {
    val enqNum = Input(UInt(3.W))
    val deqNum = Input(UInt(3.W))
      
    val remainingNum = Output(UInt(5.W))
      
    val enqDat1 = Input(UInt(16.W))
    val enqDat2 = Input(UInt(16.W))
    val enqDat3 = Input(UInt(16.W))
    val enqDat4 = Input(UInt(16.W))
      
    val deqDat1 = Output(UInt(16.W))
    val deqDat2 = Output(UInt(16.W))
    val deqDat3 = Output(UInt(16.W))
    val deqDat4 = Output(UInt(16.W))
  })
    
    val enqPtr     = RegInit(UInt(4.W), 0.U)
    val deqPtr     = RegInit(UInt(4.W), 0.U)
    val num        = RegInit(UInt(4.W), 0.U)
    val out1 = RegInit(UInt(16.W), 0.U)
    val out2 = RegInit(UInt(16.W), 0.U)
    val out3 = RegInit(UInt(16.W), 0.U)
    val out4 = RegInit(UInt(16.W), 0.U)
    val ram = Mem(16, UInt(16.W))
    
    val raddr1 = deqPtr
    val raddr2 = deqPtr + 1.U
    val raddr3 = deqPtr + 2.U
    val raddr4 = deqPtr + 3.U
    
    when (io.enqNum === 4.U) {
        ram(enqPtr) := io.enqDat1
        ram(enqPtr + 1.U) := io.enqDat2
        ram(enqPtr + 2.U) := io.enqDat3
        ram(enqPtr + 3.U) := io.enqDat4
        enqPtr := enqPtr + 4.U
    } .elsewhen (io.enqNum === 3.U) {
        ram(enqPtr) := io.enqDat1
        ram(enqPtr + 1.U) := io.enqDat2
        ram(enqPtr + 2.U) := io.enqDat3
        enqPtr := enqPtr + 3.U
    } .elsewhen (io.enqNum === 2.U) {
        ram(enqPtr) := io.enqDat1
        ram(enqPtr + 1.U) := io.enqDat2
        enqPtr := enqPtr + 2.U
    } .elsewhen (io.enqNum === 1.U) {
        ram(enqPtr) := io.enqDat1
        enqPtr := enqPtr + 1.U
    } .otherwise {
        enqPtr := enqPtr
    }

    num := num + io.enqNum - io.deqNum
    deqPtr := deqPtr + io.deqNum
    
    when ((raddr1 === enqPtr) && (io.enqNum >= 1.U)) {
        out1 := io.enqDat1
    } .elsewhen ((raddr1 === (enqPtr + 1.U)) && (io.enqNum >= 2.U)) {
        out1 := io.enqDat2
    } .elsewhen ((raddr1 === (enqPtr + 2.U)) && (io.enqNum >= 3.U)) {
        out1 := io.enqDat3
    } .elsewhen ((raddr1 === (enqPtr + 3.U)) && (io.enqNum >= 4.U)) {
        out1 := io.enqDat4
    } .otherwise {
        out1 := ram(deqPtr)
    } 
    when ((raddr2 === enqPtr) && (io.enqNum >= 1.U)) {
        out2 := io.enqDat1
    } .elsewhen ((raddr2 === (enqPtr + 1.U)) && (io.enqNum >= 2.U)) {
        out2 := io.enqDat2
    } .elsewhen ((raddr2 === (enqPtr + 2.U)) && (io.enqNum >= 3.U)) {
        out2 := io.enqDat3
    } .elsewhen ((raddr2 === (enqPtr + 3.U)) && (io.enqNum >= 4.U)) {
        out2 := io.enqDat4
    } .otherwise {
        out2 := ram(deqPtr + 1.U)
    } 
    when ((raddr3 === enqPtr) && (io.enqNum >= 1.U)) {
        out3 := io.enqDat1
    } .elsewhen ((raddr3 === (enqPtr + 1.U)) && (io.enqNum >= 2.U)) {
        out3 := io.enqDat2
    } .elsewhen ((raddr3 === (enqPtr + 2.U)) && (io.enqNum >= 3.U)) {
        out3 := io.enqDat3
    } .elsewhen ((raddr3 === (enqPtr + 3.U)) && (io.enqNum >= 4.U)) {
        out3 := io.enqDat4
    } .otherwise {
        out3 := ram(deqPtr + 2.U)
    } 
    when ((raddr4 === enqPtr) && (io.enqNum >= 1.U)) {
        out4 := io.enqDat1
    } .elsewhen ((raddr4 === (enqPtr + 1.U)) && (io.enqNum >= 2.U)) {
        out4 := io.enqDat2
    } .elsewhen ((raddr4 === (enqPtr + 2.U)) && (io.enqNum >= 3.U)) {
        out4 := io.enqDat3
    } .elsewhen ((raddr4 === (enqPtr + 3.U)) && (io.enqNum >= 4.U)) {
        out4 := io.enqDat4
    } .otherwise {
        out4 := ram(deqPtr + 3.U)
    } 
    
    io.deqDat1 := out1
    io.deqDat2 := out2
    io.deqDat3 := out3
    io.deqDat4 := out4
    io.remainingNum := num
}


// Helper Module for generating output
class Flit_Generator extends Module {
    val io = IO(new Bundle{
        val header = Input(UInt(4.W))
        val a = Input(UInt(16.W))
        val b = Input(UInt(16.W))
        val c = Input(UInt(16.W))
        val d = Input(UInt(16.W))
        val result = Output(UInt(64.W))
    })
    
    val zero = 0.U(16.W)
    when (io.header === 0.U) {
        io.result :=  Cat(io.d, Cat(io.c, Cat(io.b, io.a)))
    } .elsewhen (io.header === 1.U) {
        io.result := Cat(io.c, Cat(io.b, Cat(io.a, zero)))
    } .elsewhen (io.header === 2.U) {
        io.result := Cat(io.c, Cat(io.b, Cat(zero, io.a)))
    } .elsewhen (io.header === 3.U) {
        io.result := Cat(io.b, Cat(io.a, Cat(zero, zero)))
    } .elsewhen (io.header === 4.U) {
        io.result := Cat(io.c, Cat(zero, Cat(io.b, io.a)))
    } .elsewhen (io.header === 5.U) {
        io.result := Cat(io.b, Cat(zero, Cat(io.a, zero)))
    } .elsewhen (io.header === 6.U) {
        io.result := Cat(io.b, Cat(zero, Cat(zero, io.a)))
    } .elsewhen (io.header === 7.U) {
        io.result := Cat(io.a, Cat(zero, Cat(zero, zero)))
    } .elsewhen (io.header === 8.U) {
        io.result := Cat(zero, Cat(io.c, Cat(io.b, io.a)))
    } .elsewhen (io.header === 9.U) {
        io.result := Cat(zero, Cat(io.b, Cat(io.a, zero)))
    } .elsewhen (io.header === 10.U) {
        io.result := Cat(zero, Cat(io.b, Cat(zero, io.a)))
    } .elsewhen (io.header === 11.U) {
        io.result := Cat(zero, Cat(io.a, Cat(zero, zero)))
    } .elsewhen (io.header === 12.U) {
        io.result := Cat(zero, Cat(zero, Cat(io.b, io.a)))
    } .elsewhen (io.header === 13.U) {
        io.result := Cat(zero, Cat(zero, Cat(io.a, zero)))
    } .elsewhen (io.header === 14.U) {
        io.result := Cat(zero, Cat(zero, Cat(zero, io.a)))
    } .otherwise {
        io.result := 0.U(64.W)
    }
}


// It contains synchronous {input_fifo buffer, header, flit_counter, delayed_flit, (state, outgen_enable)},
// combinational logic {output_generator}, and synchronous {output_fifo buffer}.
// Under this design, all flits should have a 2 cycle delay
// Future work may develop secure 0/1-latency uncompressed channel.
// Note: the flit structure is {NOC header, TileLink header, Actual data}, and this only handles the actual data payload part
class Decompressor(tlbundle_params: TLBundleParameters) extends Module {
    val io = IO(new Bundle{
        val in = Flipped(Decoupled(new TLBundleD(tlbundle_params)))
        val compressed = Input(Bool())
        val head = Input(Bool())
        val tail = Input(Bool())
        val out = Decoupled(new TLBundleD(tlbundle_params))
    })
    
    val idle :: receiving :: running :: Nil = Enum(3)
    val state = RegInit(UInt(2.W), idle)
    // io.in.ready is only disabled in running stage (this also assumes that the output buffer is large enough)
    io.in.ready := !(state === running)
    // The register storing all information besides the data in the TLBundle
    val tl_info = RegInit(new TLBundleD(tlbundle_params), 0.U.asTypeOf(new TLBundleD(tlbundle_params)))
    // The register storing the encoding header
    val header = RegInit(UInt(32.W), 0.U)
    // The register for the 4-bit bitmap fed into the output_generator
    val bitmap = RegInit(UInt(4.W), "hf".asUInt(4.W))
    // The counter for number of flits that have been produced for a certain packet
    val flit_counter = RegInit(UInt(3.W), 0.U)
    // The register to store the entire flit
    val delayed_flit = RegNext(io.in.bits)
    // The register to tell if we can insert flit into the output buffer in the next cycle
    val output_enable = RegInit(Bool(), false.B)
    // The register to tell if which value to use for the output (true.B for output_generator and false.B for the delayed_flit)
    val output_sel = RegInit(Bool(), true.B)
    // The input buffer
    val input_fifo = Module(new Decompressor_Fifo)
    
    // The actual combinational logic that generates the output flits
    val output_generator = Module(new Flit_Generator)
    // Since all the input data should be fixed, directly connect them here
    output_generator.io.header := bitmap
    output_generator.io.a := input_fifo.io.deqDat1
    output_generator.io.b := input_fifo.io.deqDat2
    output_generator.io.c := input_fifo.io.deqDat3
    output_generator.io.d := input_fifo.io.deqDat4
    
    // The output buffer
    val output_fifo = Module(new Queue(new TLBundleD(tlbundle_params), 8))
    // Create a Wire of the bundle for output
    val out_bundle = Wire(new TLBundleD(tlbundle_params))
    //out_bundle.channelName := tl_info.channelName
    out_bundle.opcode := tl_info.opcode
    out_bundle.param := tl_info.param
    out_bundle.size := tl_info.size
    out_bundle.source := tl_info.source
    out_bundle.sink := tl_info.sink
    out_bundle.denied := tl_info.denied
    out_bundle.user := tl_info.user
    out_bundle.echo := tl_info.echo
    out_bundle.data := output_generator.io.result
    out_bundle.corrupt := tl_info.corrupt
    
    // The output buffer is controlled by the output_enable and fed with the mux between output_generator and delayed_flit
    output_fifo.io.enq.bits := Mux(output_sel, out_bundle, delayed_flit)
    output_fifo.io.enq.valid := output_enable
    io.out <> output_fifo.io.deq
    
    
    
    when (state === idle) { // Wait for incoming flits
        // assert(input_fifo.io.remainingNum === 0.U, "There should be no data in the input buffer in the idle state")
        when (io.in.valid) { 
            when (io.compressed) {
                when (io.head) { // Starts Only if receiving the head flit 
                    // Update the TLBundleD info
                    tl_info := io.in.bits
                    // Calculate how many non-zero values exist in the new input flit and load them to the input buffer
                    val valid_data_num = Mux(io.in.bits.data(63, 48).asUInt() > 0.U, 2.U,
                                             Mux(io.in.bits.data(47, 32).asUInt() > 0.U, 1.U, 0.U))
                    input_fifo.io.enqNum := valid_data_num
                    input_fifo.io.enqDat1 := io.in.bits.data(47, 32)
                    input_fifo.io.enqDat2 := io.in.bits.data(63, 48)
                    input_fifo.io.enqDat3 := 0.U
                    input_fifo.io.enqDat4 := 0.U
                    // Calculate how many non-zero values are needed to generate the output
                    val non_zero_num = 4.U - PopCount(io.in.bits.data(3, 0))
                    when (valid_data_num >= non_zero_num) { // Process if there's enough data
                        // Deque non_zero_num elements from the input buffer
                        input_fifo.io.deqNum := non_zero_num
                        // Enable the output, select the output_generator, and update bitmap
                        bitmap := io.in.bits.data(3, 0)
                        output_enable := true.B
                        output_sel := true.B
                        // Update header and flit_counter
                        header := io.in.bits.data(31, 0) >> 4
                        flit_counter := 1.U
                    } .otherwise { // Wait since there's not enough data to construct the first flit
                        // Deque nothing from the input buffer
                        input_fifo.io.deqNum := 0.U
                        // Disable the output and set the output_sel and bitmap as default
                        output_enable := false.B
                        output_sel := true.B
                        bitmap := "hf".asUInt(4.W)
                        // Update header but keep flit_counter zero
                        header := io.in.bits.data(31, 0)
                        flit_counter := 0.U
                    }
                    // Update state
                    when (io.tail) {
                        state := running 
                    } .otherwise {
                        state := receiving
                    }
                } .otherwise { // Do nothing as it's not the head flit
                    // Load nothing to the input buffer
                    input_fifo.io.enqNum := 0.U
                    input_fifo.io.enqDat1 := 0.U
                    input_fifo.io.enqDat2 := 0.U
                    input_fifo.io.enqDat3 := 0.U
                    input_fifo.io.enqDat4 := 0.U
                    // Deque nothing from the input buffer
                    input_fifo.io.deqNum := 0.U
                    // Disable the output and set the output_sel and bitmap as default
                    output_enable := false.B
                    output_sel := true.B
                    bitmap := "hf".asUInt(4.W)
                    // Remain in the idle state
                    state := idle
                    header := 0.U
                    flit_counter := 0.U
                }
            } .otherwise { // Let the uncompressed flit pass through
                // Load nothing to the input buffer
                input_fifo.io.enqNum := 0.U
                input_fifo.io.enqDat1 := 0.U
                input_fifo.io.enqDat2 := 0.U
                input_fifo.io.enqDat3 := 0.U
                input_fifo.io.enqDat4 := 0.U
                // Deque nothing from the input buffer
                input_fifo.io.deqNum := 0.U
                // Enable the output, select the delayed_flit, and set the bitmap as default
                output_enable := true.B
                output_sel := false.B
                bitmap := "hf".asUInt(4.W)
                // Remain in the idle state
                state := idle
                header := 0.U
                flit_counter := 0.U
            }
        } .otherwise { // Do nothing as there's no valid input
            // Load nothing to the input buffer
            input_fifo.io.enqNum := 0.U
            input_fifo.io.enqDat1 := 0.U
            input_fifo.io.enqDat2 := 0.U
            input_fifo.io.enqDat3 := 0.U
            input_fifo.io.enqDat4 := 0.U
            // Deque nothing from the input buffer
            input_fifo.io.deqNum := 0.U
            // Disable the output and set the output_sel and bitmap as default
            output_enable := false.B
            output_sel := true.B
            bitmap := "hf".asUInt(4.W)
            // Remain in the idle state
            state := idle
            header := 0.U
            flit_counter := 0.U
        }
    } .elsewhen (state === receiving) { // Receive inputs and generate outputs to the output buffer simultaenously
        when (io.in.valid && io.compressed  && (!io.head)) {
            // Calculate how many non-zero values exist in the new input flit and load them to the input buffer
            val valid_data_num = Mux(io.in.bits.data(63, 48).asUInt() > 0.U, 4.U,
                                     Mux(io.in.bits.data(47, 32).asUInt() > 0.U, 3.U,
                                         Mux(io.in.bits.data(31, 16).asUInt() > 0.U, 2.U,
                                             Mux(io.in.bits.data(15, 0).asUInt() > 0.U, 1.U, 0.U))))
            input_fifo.io.enqNum := valid_data_num
            input_fifo.io.enqDat1 := io.in.bits.data(15, 0)
            input_fifo.io.enqDat2 := io.in.bits.data(31, 16)
            input_fifo.io.enqDat3 := io.in.bits.data(47, 32)
            input_fifo.io.enqDat4 := io.in.bits.data(63, 48)
            // Calculate how many non-zero values are needed to generate the output
            val encoding = header(3, 0)
            val non_zero_num = 4.U - PopCount(encoding)
            // A new input can contains 4 non-zero values, so there should always be enough data
            // Deque non_zero_num elements from the input buffer
            input_fifo.io.deqNum := non_zero_num
            // Enable the output, select the output_generator, and set bitmap
            output_enable := true.B
            output_sel := true.B
            bitmap := encoding
            // Update header 
            header := header >> 4
            // Update state and flit_counter
            when (io.tail) {
                when (flit_counter === 7.U) {
                    state := idle
                    flit_counter := 0.U
                } .otherwise {
                    state := running
                    flit_counter := flit_counter + 1.U
                }
            } .otherwise {
                state := receiving
                flit_counter := flit_counter + 1.U
            }           
        } .otherwise { // Continue to generate output though without a valid input (ignore any uncompressed flits)
            // Load nothing to the input buffer
            input_fifo.io.enqNum := 0.U
            input_fifo.io.enqDat1 := 0.U
            input_fifo.io.enqDat2 := 0.U
            input_fifo.io.enqDat3 := 0.U
            input_fifo.io.enqDat4 := 0.U
            // Calculate how many non-zero values are needed to generate the output
            val encoding = header(3, 0)
            val non_zero_num = 4.U - PopCount(encoding)
            when (input_fifo.io.remainingNum >= non_zero_num) {
                // Deque non_zero_num elements from the input buffer
                input_fifo.io.deqNum := non_zero_num
                // Enable the output, select the output_generator, and set bitmap
                output_enable := true.B
                output_sel := true.B
                bitmap := encoding
                // Update header and flit_counter
                header := header >> 4
                flit_counter := flit_counter + 1.U
                // Remain in receiving state
                state := receiving
            } .otherwise { // Do nothing if there's not enough data
                // Deque nothing from the input buffer
                input_fifo.io.deqNum := 0.U
                // Disable the output and set the output_sel and bitmap as default
                output_enable := false.B
                output_sel := true.B
                bitmap := "hf".asUInt(4.W)
                // Remain in receiving state
                state := receiving
                header := header
                flit_counter := flit_counter
            }
        }
    } .elsewhen (state === running) { // Only generating outputs to the output buffer (Stop receiving flits after receving the tail)
        // Load nothing to the input buffer
        input_fifo.io.enqNum := 0.U
        input_fifo.io.enqDat1 := 0.U
        input_fifo.io.enqDat2 := 0.U
        input_fifo.io.enqDat3 := 0.U
        input_fifo.io.enqDat4 := 0.U
        // Calculate how many non-zero values are needed to generate the output
        val encoding = header(3, 0)
        val non_zero_num = 4.U - PopCount(encoding)
//         assert(input_fifo.io.remainingNum >= non_zero_num, "There should be enough data in the input buffer in the running state")
        // Deque non_zero_num elements from the input buffer
        input_fifo.io.deqNum := non_zero_num
        // Enable the output, select the output_generator, and set bitmap
        output_enable := true.B
        output_sel := true.B
        bitmap := encoding
        // Update header 
        header := header >> 4
        // Update state and flit_counter
        when (flit_counter === 7.U) {
            state := idle
            flit_counter := 0.U
        } .otherwise {
            state := running
            flit_counter := flit_counter + 1.U
        }
    } .otherwise { // Do nothing for edge cases, this should not execute if the hardware works properly.
        // Load nothing to the input buffer
        input_fifo.io.enqNum := 0.U
        input_fifo.io.enqDat1 := 0.U
        input_fifo.io.enqDat2 := 0.U
        input_fifo.io.enqDat3 := 0.U
        input_fifo.io.enqDat4 := 0.U
        // Deque nothing from the input buffer
        input_fifo.io.deqNum := 0.U
        // Disable the output and set the output_sel and bitmap as default
        output_enable := false.B
        output_sel := true.B
        bitmap := "hf".asUInt(4.W)
        // Return to the idle state
        state := idle
        header := 0.U
        flit_counter := 0.U
    }
    
}
