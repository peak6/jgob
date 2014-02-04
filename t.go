package main

import (
	"encoding/gob"
	"os"
)

func main() {
	enc := gob.NewEncoder(os.Stdout)
	enc.Encode(uint(6))
	enc.Encode(7)
	enc.Encode(255)
}
