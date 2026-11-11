SELECT enumlabel
FROM pg_enum
WHERE enumtypid = 'operation_enum'::regtype
ORDER BY enumsortorder;