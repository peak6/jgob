package main

import (
"encoding/gob"
"fmt"
_ "math"
"os"
)

func main() {
	enc := gob.NewEncoder(os.Stdout)

	/*	enc.Encode("hi")
		enc.Encode(uint(6))
		enc.Encode(7)
		enc.Encode(255)

		addI(math.MaxInt8, enc)
		addI(math.MaxInt16, enc)
		addI(math.MaxInt32, enc)
		addI(math.MaxInt64, enc)
		addU(math.MaxUint8, enc)
		addU(math.MaxUint16, enc)
		addU(math.MaxUint32, enc)
		addU(math.MaxUint64, enc)

		enc.Encode(true)
		enc.Encode(false)
		enc.Encode(true)

		addF(22.2, enc)
		addF(-22.2, enc)
		addF(math.SmallestNonzeroFloat32, enc)
		addF(math.MaxFloat32, enc)
		addF(math.SmallestNonzeroFloat64, enc)
		addF(123456789.123456789, enc)
	*/
	// enc.Encode([]byte("howdy"))
	enc.Encode([]string{"hello"})
	// addF(math.MaxFloat64-1.0, enc)

	// enc.Encode([3]int{1, 2, 3})
	// enc.Encode([]int{1, 2, 3})
}
func addU(x uint64, enc *gob.Encoder) {
	enc.Encode(fmt.Sprintf("%d", x))
	enc.Encode(x)
}
func addI(x int, enc *gob.Encoder) {
	enc.Encode(fmt.Sprintf("%d", x))
	enc.Encode(x)
}
func addF(x float64, enc *gob.Encoder) {
	enc.Encode(fmt.Sprintf("%f", x))
	enc.Encode(x)
}
