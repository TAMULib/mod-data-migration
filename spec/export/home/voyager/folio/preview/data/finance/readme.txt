all finance/* records should be created by POSTing to API endpoints. The endpoint name is finance/[subdirectory_name], e.g. finance/groups.
They should be posted in this order to maintain the correct hierarchy:
expense-classes, fund-types, fiscal-years, ledgers, groups, funds, and budgets.
fund-types are probably already handled as reference data, so those could be excluded.


The extract_po_voyager_xxdb.pl scripts create 2 record types, purchase orders (with embedded po lines) and pieces. The purchase orders
should be posted to /orders/composite-orders. Once that is done (which took me 36 minutes), you will need to replicate the process contained in the update_pieces.pl.
Pieces records require a title id, but the title id doesn't exist until the po is posted. So the piece records will have to be updated
with the titleId, then posted to /orders/pieces.


this zip file contains an updated version of /configurations/entries data with some more orders values
