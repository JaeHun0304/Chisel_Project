
package lif

import chisel3._

/*
  Author:JaeHun Jung
  Date: 03/22/2019
  Description: This is the hardware module that implements LIF (Leaky Integrate-and-Fire Model)
  in Chisel language. If you are not familiar with the Chisel please check https://github.com/freechipsproject/chisel3. 
*/

// There are 4 inputs and single output in this hardware class
// sj1, sj2, sj3 are signal of neuron which indicates whether it is fired(1) or not(0)
// reset signal is used to initialize the membrane potential function and three previous neuron signals
class LIF extends Module {  
	val io = IO(new Bundle {
    val reset = Input(Bool())
    val sj1, sj2, sj3 = Input(UInt(1.W))
    val firing_out = Output(Bool())
    val vi = Output(UInt(5.W))
  })

  val sj1_prev, sj2_prev, sj3_prev = RegInit(0.U(1.W))  // sj1_prev, sj2_prev, and sj3_prev registers save 1-bit Sj[t-1]
  val vi_prev = RegInit(0.U(5.W)) // vi and vi_prev registers save membrane potential at t and at (t-1) respectively
  val vi_temp = Wire(UInt(5.W)) // Temporary wiring signal for determining whether reset or not the Vi potential

  io.firing_out := 0.U   //initialize firing output waveform to zero
  vi_temp := 0.U  //Initialize wiring signal.

// When reset is treu(1), initialize registers, note that vi is initialized into resting poetntial which is 6
  when(io.reset === 1.U){  
  	io.vi := 6.U
  	vi_prev := io.vi      //Initialize Vi[t-1] with Vi[t] for t=0 case
  	sj1_prev := io.sj1 //For the first input, initialize Sj[t-1] with input of Sj[t] for t=0 case
  	sj2_prev := io.sj2
  	sj3_prev := io.sj3
  }.otherwise{ 
    // perform LIF computation if reset flag is not set
  	vi_temp := vi_prev + (sj1_prev + 2.U * sj2_prev + 3.U * sj3_prev) - 1.U
  	// When output wire signal of vi_temp memebrane potential exceeds threshold, set firing_out flag and reset Vi[t] to resting potential.
    // Otherwise, final vi output will be same with vi_temp signal value
  	io.vi := vi_temp
    // vi_temp will be '31' since equation "vi_temp := vi_prev + (sj1_prev + 2.U * sj2_prev + 3.U * sj3_prev) - 1.U" will be '-1' when 
    // all the variables are zero which makes vi_temp '31' since it is unsigned int data type. Therfore, the vi_temp should not be '31'
    when(vi_temp >= 14.U && vi_temp != 31.U){
  		io.firing_out := 1.U
  		io.vi := 6.U
  	}
    // After computation and comparison are finisehd, update Sj[t-1] and Vi[t-1] for the next calculation.
  	sj1_prev := io.sj1
  	sj2_prev := io.sj2
  	sj3_prev := io.sj3
  	vi_prev := io.vi
  }

}