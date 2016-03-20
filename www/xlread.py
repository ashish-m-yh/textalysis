from pyExcelerator import *

def xlread(arg):
        matrix = []
        last_col_idx = 0

        for sheet_name, values in parse_xls(arg, 'cp1251'):
            for row_idx, col_idx in sorted(values.keys()):
                v = values[(row_idx, col_idx)]

                if isinstance(v, unicode):
                    v = v.encode('cp866', 'backslashreplace')
                else:
                    v = str(v)

                last_col_idx = col_idx
                matrix.append(v)

        return (matrix,last_col_idx) 
