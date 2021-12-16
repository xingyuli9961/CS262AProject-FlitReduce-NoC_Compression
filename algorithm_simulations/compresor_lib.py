# This work used "compression_lib.py" by Tushar, and the link is
# https://github.com/t14916/NoC_Compression/blob/main/compression_lib.py

# 64bit base, 1 byte offset
def no_delta_compressor1(data_flits):
    base_flit = data_flits[0]
    delta_flits = []

    for flit in data_flits[1:]:
        offset = flit - base_flit
        if -128 <= offset <= 127:
            delta_flits.append(offset)
        else:
            return False, None, None

    return True, base_flit, delta_flits


# 64bit base, 2 bytes offset
def no_delta_compressor2(data_flits):
    base_flit = data_flits[0]
    delta_flits = []

    for flit in data_flits[1:]:
        offset = flit - base_flit
        if -32768 <= offset <= 32767:
            delta_flits.append(offset)
        else:
            return False, None, None

    return True, base_flit, delta_flits


# our own proposal 1
def adapted_no_delta_compressor(data_flits):
    base_flit = data_flits[0]
    offset_flits = []
    delta_flits = []
    mask = 0
    for flit in data_flits:
        offset = flit - base_flit
        if -128 <= offset <= 127:
            offset_flits.append(offset)
            mask += 1
        else:
            offset_flits.append(0)
            delta_flits.append(flit)
        mask = mask << 1
    return base_flit, offset_flits, delta_flits, mask


# our own proposal 2
def another_adapted_no_delta_compressor(data_flits):
    delta_flits = []
    data_max = max(data_flits)
    data_min = min(data_flits)
    if data_max - data_min > 256:
        return False, None, None

    base_flit = (data_max + data_min) // 2
    for flit in data_flits:
        offset = flit - base_flit
        delta_flits.append(offset)

    return True, base_flit, delta_flits


# The header of the zen_compression is an one-hot mask
def zero_compressor(data_flits):
    delta_flits = []
    mask = 0
    for flit in data_flits:
        if flit == 0:
            mask += 1
        else:
            delta_flits.append(flit)
        mask = mask << 1
    return mask, delta_flits


# The 64-byte cache line can be either segmented in 16 32-bit words and encoded.
def frequent_pattern_compressor(data_flits):
    prefix = []
    delta_flits = []
    data_size = 48
    for flit in data_flits:
        # Check for special cases
        flag1 = False
        flag2 = False
        flag3 = False
        lower_half = flit & 0x0ffff
        upper_half = (flit >> 16) & 0x0ffff
        byte1 = lower_half & 0x0ff
        byte2 = (lower_half >> 8) & 0x0ff
        byte3 = upper_half & 0x0ff
        byte4 = (upper_half >> 8) & 0x0ff
        # Check flags, will be explained later
        if upper_half == 0:
            flag1 = True
        elif (0 <= lower_half <= 255) and (0 <= upper_half <= 255):
            flag2 = True
        elif byte1 == byte2 == byte3 == byte4:
            flag3 = True

        if flit == 0:  # The data is zero
            prefix.append(0)
        elif -8 <= flit <= 7:  # The data is 4-bit sign-extended
            prefix.append(1)
            delta_flits.append(flit)
            data_size += 4
        elif -128 <= flit <= 127:  # The data is 8-bit sign-extended
            prefix.append(2)
            delta_flits.append(flit)
            data_size += 8
        elif -32768 <= flit <= 32767:  # The data is 16-bit sign-extended
            prefix.append(3)
            delta_flits.append(flit)
            data_size += 16
        elif flag1:  # The data is half-word padded with a zero upper half-word
            prefix.append(4)
            delta_flits.append(lower_half)
            data_size += 16
        elif flag2:  # The data is two half-words, each a byte sign-extended
            prefix.append(5)
            delta_flits.append(lower_half)
            delta_flits.append(upper_half)
            data_size += 16
        elif flag3:  # The data is 1 byte repeating
            prefix.append(6)
            delta_flits.append(byte4)
            data_size += 8
        else:  # Uncompressed
            prefix.append(7)
            delta_flits.append(flit)
            data_size += 32
    return data_size, prefix, delta_flits


# Omit the implementation of this algorithm as this requires a lot of overhead in the header flits. In our case,
# either it adds too many overhead bits or it doesn't fit in the current header flit.
def flit_zip(data_flits):
    return None
