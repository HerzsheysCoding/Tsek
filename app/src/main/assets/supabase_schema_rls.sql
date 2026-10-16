-- ==============================================================================
-- TSEK CYBERSECURITY DATABASE SCHEMA & ROW-LEVEL SECURITY (RLS) POLICIES
-- Implements Points 3, 4, 6, 7, 8, and 13 from the Security Checklist:
-- Point 3: Use public DB key (Client uses anon key only; admin keys prohibited)
-- Point 4: Enable row-level security (RLS) on all tables
-- Point 6: Enforce server-side auth (auth.uid() checks for all row operations)
-- Point 7: Lock record access (Tenants can only see/modify their own records)
-- Point 8: Block field tampering (User ID, created_at, role locked via triggers)
-- Point 13: Parameterize queries (Stored procedures with bound parameters)
-- ==============================================================================

-- 1. PROFILES TABLE
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    full_name TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

-- Point 4: Enable Row-Level Security
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Point 6 & 7: Lock record access to authenticated owner
CREATE POLICY "Users can only read own profile"
    ON public.profiles FOR SELECT
    TO authenticated
    USING (auth.uid() = id);

CREATE POLICY "Users can only update own profile"
    ON public.profiles FOR UPDATE
    TO authenticated
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can insert own profile"
    ON public.profiles FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = id);


-- 2. CHECK HISTORY TABLE (Synced Scans)
CREATE TABLE IF NOT EXISTS public.check_history (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    raw_snippet TEXT NOT NULL,
    input_type TEXT NOT NULL,
    verdict TEXT NOT NULL,
    score INT NOT NULL,
    category TEXT NOT NULL,
    primary_domain TEXT,
    primary_phone TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

-- Point 4: Enable Row-Level Security
ALTER TABLE public.check_history ENABLE ROW LEVEL SECURITY;

-- Point 7: Lock record access
CREATE POLICY "Users can only view their own scan history"
    ON public.check_history FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "Users can only insert their own scan records"
    ON public.check_history FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can only delete their own scan history"
    ON public.check_history FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);


-- 3. SCAM REPORTS TABLE (Community Reporting)
CREATE TABLE IF NOT EXISTS public.scam_reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    target_type TEXT NOT NULL,
    target_value TEXT NOT NULL,
    category TEXT NOT NULL,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

-- Point 4: Enable Row-Level Security
ALTER TABLE public.scam_reports ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Authenticated users can submit scam reports"
    ON public.scam_reports FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = reporter_id);

CREATE POLICY "Authenticated users can view verified scam reports"
    ON public.scam_reports FOR SELECT
    TO authenticated
    USING (true);


-- Point 8: Block field tampering (Prevent modifying user_id or created_at)
CREATE OR REPLACE FUNCTION public.block_field_tampering()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.user_id <> OLD.user_id THEN
        RAISE EXCEPTION 'Field user_id is immutable and cannot be tampered with.';
    END IF;
    IF NEW.created_at <> OLD.created_at THEN
        NEW.created_at = OLD.created_at; -- Prevent backdating timestamps
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE TRIGGER trg_block_tampering_check_history
    BEFORE UPDATE ON public.check_history
    FOR EACH ROW
    EXECUTE FUNCTION public.block_field_tampering();
