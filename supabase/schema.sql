-- Cloud mirror of the local SQLDelight schema. Every row belongs to one user; row-level security
-- guarantees users can only read and write their own rows. Safe to re-run.

create table if not exists public.client (
    user_id uuid not null default auth.uid() references auth.users(id) on delete cascade,
    id text not null,
    name text not null,
    email text not null,
    kvk_number text not null,
    btw_number text not null,
    payment_term_days integer not null,
    updated_at bigint not null,
    version bigint not null,
    primary key (user_id, id)
);

create table if not exists public.invoice (
    user_id uuid not null default auth.uid() references auth.users(id) on delete cascade,
    id text not null,
    number text not null,
    client_id text not null,
    client_name text not null,
    status text not null,
    issue_date text not null,
    due_date text not null,
    updated_at bigint not null,
    version bigint not null,
    primary key (user_id, id),
    unique (user_id, number)
);

create table if not exists public.invoice_line (
    user_id uuid not null default auth.uid() references auth.users(id) on delete cascade,
    invoice_id text not null,
    position integer not null,
    description text not null,
    quantity_milli bigint not null,
    unit_price_cents bigint not null,
    vat_percent integer not null,
    primary key (user_id, invoice_id, position),
    foreign key (user_id, invoice_id) references public.invoice (user_id, id) on delete cascade
);

create table if not exists public.business_profile (
    user_id uuid not null default auth.uid() references auth.users(id) on delete cascade primary key,
    name text not null,
    address text not null,
    kvk_number text not null,
    btw_number text not null,
    iban text not null,
    default_payment_term_days integer not null,
    updated_at bigint not null,
    version bigint not null
);

do $$
declare t text;
begin
    foreach t in array array['client', 'invoice', 'invoice_line', 'business_profile'] loop
        execute format('alter table public.%I enable row level security', t);
        execute format('drop policy if exists "own rows" on public.%I', t);
        execute format(
            'create policy "own rows" on public.%I for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid())',
            t
        );
    end loop;
end $$;
