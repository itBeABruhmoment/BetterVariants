import pandas as pd
import os
import json

schema = {
    'variantID': str,
    'officerInfo': str,
    'smods': str
}

variants_csv = pd.read_csv('../data/variants_lib/variant_tags.csv', dtype=schema, index_col='variantID')
variants_csv = variants_csv[variants_csv.index.notna()]
print(variants_csv)
# variants_csv = variants_csv.dropna()
print(variants_csv)

bv_variants = variants_csv[variants_csv.index.str.contains('_bv')]
not_bv_variants = variants_csv[~variants_csv.index.str.contains('_bv')]

variant_ids = []
for path, currentDirectory, files in os.walk("../data/variants"):
	for file in files:
		name = file.split(".")
		if name[1] == 'variant':
			variant_ids.append(name[0])

for id in variant_ids:
    if not (id in bv_variants.index):
        new_row = {'variantID':id, 'officerInfo':pd.NA, 'smods':pd.NA}
        bv_variants.loc[id] = new_row

bv_variants = bv_variants.sort_index()
not_bv_variants = not_bv_variants.sort_index()

bv_variants = bv_variants.append(not_bv_variants)

bv_variants.to_csv('out.csv')
print(bv_variants)